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
import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.repository.MemberRepository;
import com.gymflow.gymflow.member.service.MemberService;
import com.gymflow.gymflow.notification.entity.NotificationTemplate;
import com.gymflow.gymflow.notification.repository.NotificationTemplateRepository;
import com.gymflow.gymflow.notification.service.NotificationService;
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
    public Member registerMember(MemberJoinRequest request) {

        log.info("Registering new member for gym Id: {}", request.getGymId());

        Gym gym = gymRepository.findById(request.getGymId())
                .orElseThrow(() -> new GymNotFoundException("Invalid Gym ID: " + request.getGymId()));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new PlanNotFoundException("Selected plan not found"));

        LocalDate start = (request.getStartDate() != null) ? request.getStartDate() : LocalDate.now();

        // STEP 1: Create Member (NO PAYMENT LOGIC HERE)
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
                .gym(gym)
                .status("PENDING") // temporary
                .build();

        Member savedMember = memberRepository.save(member);

        log.info("Member created with id={}", savedMember.getId());

        // STEP 2: Create Subscription
        var subscription = subscriptionService.createSubscription(
                savedMember.getId(),
                plan.getId(),
                gym.getId(),
                request.getInitialPayment() != null
                        ? BigDecimal.valueOf(request.getInitialPayment())
                        : BigDecimal.ZERO
        );

        // STEP 3: Create Payment (only if amount > 0)
        if (request.getInitialPayment() != null && request.getInitialPayment() > 0) {
            paymentService.addPayment(
                    savedMember.getId(),
                    subscription.getId(),
                    gym.getId(),
                    BigDecimal.valueOf(request.getInitialPayment()),
                    request.getPaymentMode(),
                    request.getTransactionRef()
            );
        }

        // STEP 4: Update Member Snapshot
        savedMember.setCurrentPlan(plan);
        savedMember.setSubscriptionStartDate(subscription.getStartDate());
        savedMember.setExpiryDate(subscription.getEndDate());
        savedMember.setStatus(subscription.getStatus());

        memberRepository.save(savedMember);

        log.info("Member subscription + payment completed");

        // STEP 5: Send Welcome Notification
        NotificationTemplate welcomeTemplate = notificationTemplateRepository.findByName("WELCOME")
                .orElseThrow(() -> new NotificationTemplateNotFoundException("Welcome template not found"));

        notificationService.sendNotification(savedMember.getId(), welcomeTemplate.getId());



        return savedMember;
    }


    @Override
    @Transactional
    public void renewSubscription(Long memberId, Long planId,
                                  Double amountPaid,
                                  String paymentMode,
                                  String transactionRef) {

        log.info("Renewing subscription for memberId: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found"));

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException("Plan not found"));

        BigDecimal paid = amountPaid != null ? BigDecimal.valueOf(amountPaid) : BigDecimal.ZERO;

        // STEP 1: Create NEW subscription (DO NOT MODIFY OLD)
        var subscription = subscriptionService.createSubscription(
                memberId,
                planId,
                member.getGym().getId(),
                paid
        );

        // STEP 2: Add payment
        if (paid.compareTo(BigDecimal.ZERO) > 0) {
            paymentService.addPayment(
                    memberId,
                    subscription.getId(),
                    member.getGym().getId(),
                    paid,
                    paymentMode,
                    transactionRef
            );
        }

        // STEP 3: Update member snapshot
        member.setCurrentPlan(plan);
        member.setSubscriptionStartDate(subscription.getStartDate());
        member.setExpiryDate(subscription.getEndDate());
        member.setStatus(subscription.getStatus());

        memberRepository.save(member);

        log.info("Membership renewed successfully for memberId={}", memberId);
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
    public Member getMemberById(Long id) {
        log.info("Fetching member by id: {}", id);
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + id));
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
                .build();
    }
}