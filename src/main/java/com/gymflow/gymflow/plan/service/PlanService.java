package com.gymflow.gymflow.plan.service;

import com.gymflow.gymflow.plan.dto.PlanDTO;

import java.util.List;

public interface PlanService {
    PlanDTO createPlan(Long gymId, PlanDTO dto);
    List<PlanDTO> getPlansByGym(Long gymId);
    PlanDTO getPlanById(Long planId);
    PlanDTO updatePlan(Long planId, PlanDTO dto);
    void deletePlan(Long planId);
    List<PlanDTO> getPlanReport(Long gymId); // includes inactive plans
}