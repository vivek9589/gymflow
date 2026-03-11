package com.gymflow.gymflow.member.service.impl;

import com.gymflow.gymflow.common.exception.BusinessException;
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
import com.gymflow.gymflow.plan.entity.Plan;
import com.gymflow.gymflow.plan.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final GymRepository gymRepository; // From gym module
    private final PlanRepository planRepository;
    private final NotificationService notificationService;
    private final NotificationTemplateRepository notificationTemplateRepository;



    @Override
    public Member registerMember(MemberJoinRequest request) {
        // 1. Validate Gym
        Gym gym = gymRepository.findById(Long.parseLong(request.getGymCode()))
                .orElseThrow(() -> new BusinessException("Invalid Gym ID: " + request.getGymCode()));

        // 2. Validate Plan
        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new BusinessException("Selected plan not found"));

        // 3. Map Admission Form Fields
        Member member = new Member();
        member.setName(request.getName());
        member.setPhone(request.getPhone());
        member.setEmail(request.getEmail());
        member.setBloodGroup(request.getBloodGroup());
        member.setWeight(request.getWeight());
        member.setHeight(request.getHeight());
        member.setDob(request.getDob());
        member.setOccupation(request.getOccupation());
        member.setFatherName(request.getFatherName());
        member.setPermanentAddress(request.getPermanentAddress());
        member.setMedicalConditions(request.getMedicalConditions());
        member.setInitialPayment(request.getInitialPayment());

        member.setGym(gym);
        member.setCurrentPlan(plan);

        // 4. Logic: Set dates
        member.setSubscriptionStartDate(LocalDate.now());
        member.setExpiryDate(LocalDate.now().plusDays(plan.getDurationInDays()));

        // 5. Status Management
        member.setStatus("PENDING"); // owner verifies payment before ACTIVE

        Member savedMember = memberRepository.save(member);

        // 6. Trigger Welcome Notification using persisted template
        NotificationTemplate welcomeTemplate = notificationTemplateRepository.findByName("WELCOME")
                .orElseThrow(() -> new BusinessException("Welcome template not found"));

        notificationService.sendNotification(savedMember.getId(), welcomeTemplate.getId());

        return savedMember;

    }




    @Transactional
    public void renewSubscription(Long memberId, Long planId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("Member not found"));
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("Plan not found"));

        // Industry Standard: If they renew early, add days to the existing expiry
        LocalDate startDate = member.getExpiryDate().isAfter(LocalDate.now())
                ? member.getExpiryDate()
                : LocalDate.now();

        member.setExpiryDate(startDate.plusDays(plan.getDurationInDays()));
        member.setStatus("ACTIVE");
        member.setCurrentPlan(plan);

        memberRepository.save(member);
    }

    @Override
    public List<Member> getAllMembersByGym(Long gymId) {
        return memberRepository.findByGymId(gymId);
    }

    @Transactional
    public void deleteMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException("Member not found with id: " + memberId);
        }
        memberRepository.deleteById(memberId);
    }

    @Override
    @Transactional
    public MemberResponse updateMember(Long id, MemberUpdateRequest request) {
        // 1. Find the existing member or throw a clean business exception
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Member not found with id: " + id));

        // 2. Map updated fields from DTO to Entity
        member.setName(request.getName()); //
        member.setPhone(request.getPhone()); //
        member.setEmail(request.getEmail()); //
        member.setWeight(request.getWeight()); //
        member.setHeight(request.getHeight()); //
        member.setPermanentAddress(request.getPermanentAddress()); //
        member.setOccupation(request.getOccupation()); //
        member.setMedicalConditions(request.getMedicalConditions()); //

        // Allow owner to manually override status if provided in the request
        if (request.getStatus() != null) {
            member.setStatus(request.getStatus());
        }

        // 3. Save and return as a clean Response DTO
        Member updatedMember = memberRepository.save(member);
        return mapToResponse(updatedMember);
    }


    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + id));
    }


    @Override
    public List<Member> searchMembers(Long gymId, String query) {
        // Prevent searching with empty or very short strings
        if (query == null || query.trim().length() < 2) {
            return Collections.emptyList();
        }

        // Calling the custom query we added to MemberRepository
        return memberRepository.searchMembersByGym(gymId, query.trim());
    }


    private MemberResponse mapToResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .name(member.getName()) //
                .phone(member.getPhone()) //
                .email(member.getEmail()) //
                .status(member.getStatus()) //
                .bloodGroup(member.getBloodGroup()) //
                .weight(member.getWeight()) //
                .height(member.getHeight()) //
                .occupation(member.getOccupation()) //
                .permanentAddress(member.getPermanentAddress()) //
                .medicalConditions(member.getMedicalConditions()) //
                .registrationDate(member.getRegistrationDate()) //
                .expiryDate(member.getExpiryDate()) //
                .initialPayment(member.getInitialPayment()) //
                // Extract the plan name safely to avoid NullPointerException
                .planName(member.getCurrentPlan() != null ? member.getCurrentPlan().getName() : "No Active Plan")
                .build();
    }
}