package com.gymflow.gymflow.member.service.impl;

import com.gymflow.gymflow.common.exception.BusinessException;
import com.gymflow.gymflow.gym.entity.Gym;
import com.gymflow.gymflow.gym.repository.GymRepository;
import com.gymflow.gymflow.member.dto.request.MemberJoinRequest;
import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.repository.MemberRepository;
import com.gymflow.gymflow.member.service.MemberService;
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

    @Override
    @Transactional
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
        // Set to PENDING so the owner can verify the payment before making them ACTIVE
        member.setStatus("PENDING");

        return memberRepository.save(member);
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
    public List<Member> searchMembers(Long gymId, String query) {
        // Prevent searching with empty or very short strings
        if (query == null || query.trim().length() < 2) {
            return Collections.emptyList();
        }

        // Calling the custom query we added to MemberRepository
        return memberRepository.searchMembersByGym(gymId, query.trim());
    }
}