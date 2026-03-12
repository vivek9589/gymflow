package com.gymflow.gymflow.plan.service.impl;

import com.gymflow.gymflow.common.exception.GymNotFoundException;
import com.gymflow.gymflow.common.exception.PlanAlreadyExistsException;
import com.gymflow.gymflow.common.exception.PlanNotFoundException;
import com.gymflow.gymflow.gym.entity.Gym;
import com.gymflow.gymflow.gym.repository.GymRepository;
import com.gymflow.gymflow.plan.dto.PlanDTO;
import com.gymflow.gymflow.plan.entity.Plan;
import com.gymflow.gymflow.plan.repository.PlanRepository;
import com.gymflow.gymflow.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final GymRepository gymRepository;

    @Override
    @Transactional
    public PlanDTO createPlan(Long gymId, PlanDTO dto) {
        log.info("Creating new plan '{}' for gymId={}", dto.getName(), gymId);

        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new GymNotFoundException("Gym not found with id: " + gymId));

        boolean exists = planRepository.findByGymId(gymId).stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(dto.getName()) && p.isActive());
        if (exists) {
            log.warn("Duplicate plan creation attempt: {}", dto.getName());
            throw new PlanAlreadyExistsException("Plan with name '" + dto.getName() + "' already exists");
        }

        Plan plan = Plan.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .durationInDays(dto.getDurationInDays())
                .description(dto.getDescription())
                .gym(gym)
                .isActive(true)
                .build();

        Plan saved = planRepository.save(plan);
        log.info("Plan '{}' created successfully with id={}", saved.getName(), saved.getId());
        return mapToDTO(saved);
    }

    @Override
    public List<PlanDTO> getPlansByGym(Long gymId) {
        log.info("Fetching active plans for gymId={}", gymId);
        return planRepository.findByGymIdAndIsActiveTrue(gymId)
                .stream().map(this::mapToDTO).toList();
    }

    @Override
    public PlanDTO getPlanById(Long planId) {
        log.info("Fetching plan details for planId={}", planId);
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException("Plan not found with id: " + planId));
        return mapToDTO(plan);
    }

    @Override
    @Transactional
    public PlanDTO updatePlan(Long planId, PlanDTO dto) {
        log.info("Updating planId={} with new details", planId);

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException("Plan not found with id: " + planId));

        plan.setName(dto.getName());
        plan.setPrice(dto.getPrice());
        plan.setDurationInDays(dto.getDurationInDays());
        plan.setDescription(dto.getDescription());
        plan.setActive(dto.isActive());

        Plan updated = planRepository.save(plan);
        log.info("Plan '{}' updated successfully", updated.getName());
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deletePlan(Long planId) {
        log.info("Soft deleting planId={}", planId);

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException("Plan not found with id: " + planId));

        plan.setActive(false);
        planRepository.save(plan);
        log.info("Plan '{}' marked inactive", plan.getName());
    }

    @Override
    public List<PlanDTO> getPlanReport(Long gymId) {
        log.info("Fetching plan report (active + inactive) for gymId={}", gymId);
        return planRepository.findByGymId(gymId)
                .stream().map(this::mapToDTO).toList();
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