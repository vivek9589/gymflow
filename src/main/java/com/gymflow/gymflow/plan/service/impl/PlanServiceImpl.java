package com.gymflow.gymflow.plan.service.impl;

import com.gymflow.gymflow.common.exception.BusinessException;
import com.gymflow.gymflow.gym.entity.Gym;
import com.gymflow.gymflow.gym.repository.GymRepository;
import com.gymflow.gymflow.plan.dto.PlanDTO;
import com.gymflow.gymflow.plan.entity.Plan;
import com.gymflow.gymflow.plan.repository.PlanRepository;
import com.gymflow.gymflow.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final GymRepository gymRepository;

    @Override
    @Transactional
    public PlanDTO createPlan(Long gymId, PlanDTO dto) {
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new BusinessException("Gym not found"));

        Plan plan = new Plan();
        plan.setName(dto.getName());
        plan.setPrice(dto.getPrice());
        plan.setDurationInDays(dto.getDurationInDays());
        plan.setDescription(dto.getDescription());
        plan.setGym(gym);
        plan.setActive(true);

        return mapToDTO(planRepository.save(plan));
    }

    @Override
    public List<PlanDTO> getPlansByGym(Long gymId) {
        return planRepository.findByGymIdAndIsActiveTrue(gymId)
                .stream().map(this::mapToDTO).toList();
    }

    @Override
    @Transactional
    public PlanDTO updatePlan(Long planId, PlanDTO dto) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("Plan not found"));

        // Update fields
        plan.setName(dto.getName());
        plan.setPrice(dto.getPrice());
        plan.setDurationInDays(dto.getDurationInDays());
        plan.setDescription(dto.getDescription());
        // Active status bhi update kar sakte hain agar owner manually disable karna chahe
        plan.setActive(dto.isActive());

        return mapToDTO(planRepository.save(plan));
    }

    @Override
    @Transactional
    public void deletePlan(Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("Plan not found"));

        // Soft Delete: Sirf active flag false karenge taaki history bani rahe
        plan.setActive(false);
        planRepository.save(plan);
    }

    private PlanDTO mapToDTO(Plan plan) {
        PlanDTO dto = new PlanDTO();
        dto.setId(plan.getId());
        dto.setName(plan.getName());
        dto.setPrice(plan.getPrice());
        dto.setDurationInDays(plan.getDurationInDays());
        dto.setDescription(plan.getDescription());
        dto.setActive(plan.isActive());
        return dto;
    }
}