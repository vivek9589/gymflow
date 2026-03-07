package com.gymflow.gymflow.plan.repository;


import com.gymflow.gymflow.plan.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByGymIdAndIsActiveTrue(Long gymId);
    List<Plan> findByGymId(Long gymId);
}