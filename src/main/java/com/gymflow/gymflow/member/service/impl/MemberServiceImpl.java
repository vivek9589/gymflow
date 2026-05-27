package com.gymflow.gymflow.member.service.impl;

import com.gymflow.gymflow.common.exception.MemberNotFoundException;
import com.gymflow.gymflow.common.exception.PlanNotFoundException;
import com.gymflow.gymflow.common.exception.GymNotFoundException;
import com.gymflow.gymflow.common.exception.NotificationTemplateNotFoundException;
import com.gymflow.gymflow.gym.entity.Gym;
import com.gymflow.gymflow.gym.repository.GymRepository;
import com.gymflow.gymflow.member.dto.request.MemberJoinRequest;
import com.gymflow.gymflow.member.dto.request.MemberUpdateRequest;
import com.gymflow.gymflow.member.dto.response.MemberResponse;
import com.gymflow.gymflow.member.dto.response.PaymentHistoryDTO;
import com.gymflow.gymflow.member.dto.response.RenewalOptionDTO;
import com.gymflow.gymflow.member.dto.response.SubscriptionHistoryDTO;
import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.repository.MemberRepository;
import com.gymflow.gymflow.member.service.MemberService;
import com.gymflow.gymflow.notification.entity.NotificationTemplate;
import com.gymflow.gymflow.notification.repository.NotificationTemplateRepository;
import com.gymflow.gymflow.notification.service.NotificationService;
import com.gymflow.gymflow.payment.entity.Subscription;
import com.gymflow.gymflow.payment.service.PaymentService;
import com.gymflow.gymflow.payment.service.SubscriptionService;
import com.gymflow.gymflow.plan.entity.Plan;
import com.gymflow.gymflow.plan.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of MemberService with business logic
 * for registration, subscription renewal, updates, and search.
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final GymRepository gymRepository;
    private final PlanRepository planRepository;
    private final NotificationService notificationService;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final SubscriptionService subscriptionService;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public MemberResponse registerMember(MemberJoinRequest request) {
        log.info("Processing flat billing registration for gym Id: {}", request.getGymId());

        Gym gym = gymRepository.findById(request.getGymId())
                .orElseThrow(() -> new GymNotFoundException("Invalid Gym ID: " + request.getGymId()));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new PlanNotFoundException("Selected plan not found"));

        LocalDate start = (request.getStartDate() != null) ? request.getStartDate() : LocalDate.now();

        // Calculate binary verification status from both incoming request modes
        boolean isPaid = request.isPaid() ||
                (request.getInitialPayment() != null && request.getInitialPayment().compareTo(BigDecimal.ZERO) > 0);

        // 1. Persist new athlete entity mapping access lifecycle flag
        Member member = Member.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .bloodGroup(request.getBloodGroup())
                .weight(request.getWeight())
                .height(request.getHeight())
                .dob(request.getDob())
                .occupation(request.getOccupation())
                .fatherName(request.getFatherName())
                .permanentAddress(request.getPermanentAddress())
                .medicalConditions(request.getMedicalConditions())
                .initialPayment(request.getInitialPayment()) // Populated missing mapping field
                .gym(gym)
                .registrationDate(LocalDate.now()) // Safe fallback value before Hibernate transaction commits
                .status(isPaid ? "ACTIVE" : "PENDING")
                .checkInToken(java.util.UUID.randomUUID().toString())
                .build();

        Member savedMember = memberRepository.save(member);

        // 2. Build Unsplitted Flat Subscription
        Subscription subscription = subscriptionService.createSubscription(
                savedMember.getId(),
                plan.getId(),
                gym.getId(),
                isPaid,
                start
        );

        // 3. Create full upfront transaction ledger if paid tracking criteria evaluates to true
        if (isPaid) {
            BigDecimal paymentAmount = (request.getInitialPayment() != null && request.getInitialPayment().compareTo(BigDecimal.ZERO) > 0)
                    ? request.getInitialPayment()
                    : plan.getPrice();

            paymentService.addPayment(
                    savedMember.getId(),
                    subscription.getId(),
                    gym.getId(),
                    paymentAmount,
                    request.getPaymentMode(),
                    request.getTransactionRef()
            );
        }

        // 4. Update core status and caching snapshots
        savedMember.setCurrentPlan(plan);
        savedMember.setSubscriptionStartDate(subscription.getStartDate());
        savedMember.setExpiryDate(subscription.getEndDate());
        savedMember.setStatus(subscription.getStatus());

        // Using saveAndFlush forces Hibernate to sync timestamps with the database immediately
        Member finalSavedMember = memberRepository.saveAndFlush(savedMember);

        log.info("Streamlined member registration cycle complete for system ID: {}", finalSavedMember.getId());

        // 5. Fire notifications context safely
        try {
            if ("ACTIVE".equals(finalSavedMember.getStatus())) {
                notificationTemplateRepository.findByName("WELCOME")
                        .ifPresent(template -> notificationService.sendNotification(finalSavedMember.getId(), template.getId()));
            }
        } catch (Exception e) {
            log.error("Guarded welcome message thread dispatch failure for ID: {}", finalSavedMember.getId(), e);
        }

        return convertToMemberResponse(finalSavedMember);
    }

    private MemberResponse convertToMemberResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .phone(member.getPhone())
                .email(member.getEmail())
                .bloodGroup(member.getBloodGroup())
                .weight(member.getWeight())
                .height(member.getHeight())
                .occupation(member.getOccupation())
                .permanentAddress(member.getPermanentAddress())
                .medicalConditions(member.getMedicalConditions())
                .status(member.getStatus())
                .expiryDate(member.getExpiryDate())
                .registrationDate(member.getRegistrationDate()) // Mapped missing field
                .initialPayment(member.getInitialPayment())   // Mapped missing field
                .planName(member.getCurrentPlan() != null ? member.getCurrentPlan().getName() : null) // Mapped missing field
                .checkInToken(member.getCheckInToken())
                .build();
    }


    @Override
    @Transactional
    public void renewSubscription(Long memberId, Long planId,
                                  BigDecimal amountPaid,
                                  String paymentMode,
                                  String transactionRef) {
        log.info("Renewing subscription for memberId={} planId={}", memberId, planId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found"));

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException("Plan not found"));

        // Early renewal date calculation logic
        LocalDate baseStartDate = LocalDate.now();
        if (member.getExpiryDate() != null && member.getExpiryDate().isAfter(LocalDate.now())) {
            baseStartDate = member.getExpiryDate();
        }

        boolean isPaid = (amountPaid != null && amountPaid.compareTo(BigDecimal.ZERO) > 0);

        // Create new subscription record tracking
        Subscription subscription = subscriptionService.createSubscription(
                memberId,
                planId,
                member.getGym().getId(),
                isPaid,
                baseStartDate
        );

        // Record transaction statement history log
        if (isPaid) {
            paymentService.addPayment(
                    memberId,
                    subscription.getId(),
                    member.getGym().getId(),
                    amountPaid,
                    paymentMode,
                    transactionRef
            );
        }

        // Update entity database state variables
        member.setCurrentPlan(plan);
        member.setSubscriptionStartDate(subscription.getStartDate());
        member.setExpiryDate(subscription.getEndDate());
        member.setStatus(subscription.getStatus());

        memberRepository.save(member);

        log.info("Renewal complete for memberId={} newExpiry={}", memberId, subscription.getEndDate());
        // No return statement here, method safely terminates execution frame
    }


    @Override
    public Page<Member> getAllMembersByGym(Long gymId, int page, int size, String status, String search, String planName) {
        log.info("Fetching paged members for gymId: {} [Page: {}, Size: {}]", gymId, page, size);

        // Sort by most recent first
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Convert "ALL" to null so the query ignores the filter
        String statusFilter = "ALL".equalsIgnoreCase(status) ? null : status;
        String planFilter = "ALL".equalsIgnoreCase(planName) ? null : planName;

        return memberRepository.findWithFilters(gymId, statusFilter, search, planFilter, pageable);
    }


    @Override
    @Transactional
    public void deleteMember(Long memberId) {
        log.info("Soft-deleting member with id: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));

        // Manual Soft Delete
        member.setDeleted(true);

        // Just saving is enough; the record stays in DB but your
        // findWithFilters query will now ignore it.
        memberRepository.save(member);
    }


    @Override
    @Transactional
    public MemberResponse updateMember(Long id, MemberUpdateRequest request) {
        log.info("Updating member with id: {}", id);

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + id));

        member.setName(request.getName());
        member.setPhone(request.getPhone());
        member.setEmail(request.getEmail());
        member.setWeight(request.getWeight());
        member.setHeight(request.getHeight());
        member.setPermanentAddress(request.getPermanentAddress());
        member.setOccupation(request.getOccupation());
        member.setMedicalConditions(request.getMedicalConditions());

        if (request.getStatus() != null) {
            member.setStatus(request.getStatus());
        }

        Member updatedMember = memberRepository.save(member);
        return mapToResponse(updatedMember);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse getMemberById(Long id) {
        log.info("Fetching member profile for id={}", id);

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + id));

        // Fetch raw payments and map to DTO
        List<PaymentHistoryDTO> history = paymentService.getPaymentsForMember(member.getId())
                .stream()
                .map(p -> PaymentHistoryDTO.builder()
                        .id(p.getId())
                        .amount(p.getAmount())
                        .paymentMode(p.getPaymentMode())
                        .status(p.getStatus())
                        .transactionRef(p.getTransactionRef())
                        .createdAt(p.getCreatedAt())
                        .build())
                .toList();

        // 1. Fetch the subscription records from the database
        List<Subscription> subscriptions = subscriptionService.getSubscriptionsForMember(member.getId());

// 2. Map them cleanly to your DTO by looking up the plan names on the fly
        List<SubscriptionHistoryDTO> planHistory = subscriptions.stream()
                .map(s -> {
                    // Find the plan name by its ID, fallback to "Unknown Plan" if it can't be found
                    String actualPlanName = planRepository.findById(s.getPlanId())
                            .map(Plan::getName)
                            .orElse("Unknown Plan");

                    return SubscriptionHistoryDTO.builder()
                            .planName(actualPlanName) // Cleanly resolved plan name string
                            .startDate(s.getStartDate())
                            .endDate(s.getEndDate())
                            .status(s.getStatus())
                            .build();
                })
                .toList();

        // Build renewal options from PlanRepository
        List<RenewalOptionDTO> renewalOptions = planRepository.findByGymId(member.getGym().getId())
                .stream()
                .filter(Plan::isActive)
                .map(plan -> RenewalOptionDTO.builder()
                        .planId(plan.getId())
                        .name(plan.getName())
                        .price(plan.getPrice())
                        .durationInDays(plan.getDurationInDays())
                        .build())
                .toList();

        return MemberResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .phone(member.getPhone())
                .email(member.getEmail())
                .bloodGroup(member.getBloodGroup())
                .weight(member.getWeight())
                .height(member.getHeight())
                .occupation(member.getOccupation())
                .permanentAddress(member.getPermanentAddress())
                .medicalConditions(member.getMedicalConditions())
                .status(member.getStatus())
                .registrationDate(member.getRegistrationDate())
                .expiryDate(member.getExpiryDate())
                .initialPayment(member.getInitialPayment())
                .planName(member.getCurrentPlan() != null ? member.getCurrentPlan().getName() : "No Active Plan")
                .checkInToken(member.getCheckInToken())
                .nextDueDate(member.getExpiryDate())
                // keep digital access pass link if required
               //.digitalAccessPassLink("https://gymflow.com/access/" + member.getCheckInToken())
                .paymentHistory(history)
                .planHistory(planHistory)
                .renewalOptions(renewalOptions)
                .build();
    }




    @Override
    public List<Member> searchMembers(Long gymId, String query) {
        log.info("Searching members in gymId: {} with query: {}", gymId, query);
        if (query == null || query.trim().length() < 2) {
            return Collections.emptyList();
        }
        return memberRepository.searchMembersByGym(gymId, query.trim());
    }

    private MemberResponse mapToResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .phone(member.getPhone())
                .email(member.getEmail())
                .status(member.getStatus())
                .bloodGroup(member.getBloodGroup())
                .weight(member.getWeight())
                .height(member.getHeight())
                .occupation(member.getOccupation())
                .permanentAddress(member.getPermanentAddress())
                .medicalConditions(member.getMedicalConditions())
                .registrationDate(member.getRegistrationDate())
                .expiryDate(member.getExpiryDate())
                .initialPayment(member.getInitialPayment())
                .planName(member.getCurrentPlan() != null ? member.getCurrentPlan().getName() : "No Active Plan")
                .checkInToken(member.getCheckInToken())
                .build();
    }
}