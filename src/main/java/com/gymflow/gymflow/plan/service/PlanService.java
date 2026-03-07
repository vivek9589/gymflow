package com.gymflow.gymflow.plan.service;

import com.gymflow.gymflow.plan.dto.PlanDTO;

import java.util.List;

public interface PlanService {
    List<PlanDTO> getPlansByGym(Long gymId);
    PlanDTO createPlan(Long gymId, PlanDTO dto);

    PlanDTO updatePlan(Long planId, PlanDTO dto);
    void deletePlan(Long planId);
}