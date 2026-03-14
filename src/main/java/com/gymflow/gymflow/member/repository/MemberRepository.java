package com.gymflow.gymflow.member.repository;



import com.gymflow.gymflow.dashboard.dto.PopularPlanDTO;
import com.gymflow.gymflow.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // Basic finders
    List<Member> findByGymId(Long gymId);
    List<Member> findByGymIdAndStatus(Long gymId, String status);

    long countByGymId(Long gymId);
    long countByGymIdAndStatus(Long gymId, String status);

    List<Member> findTop5ByGymIdOrderByCreatedAtDesc(Long gymId);

    List<Member> findByExpiryDate(LocalDate expiryDate);
    List<Member> findByGymIdAndExpiryDateBetween(Long gymId, LocalDate start, LocalDate end);

    // Monthly revenue calculation
    @Query("SELECT COALESCE(SUM(m.initialPayment), 0) " +
            "FROM Member m " +
            "WHERE m.gym.id = :gymId AND FUNCTION('MONTH', m.registrationDate) = :month")
    BigDecimal calculateMonthlyRevenue(@Param("gymId") Long gymId, @Param("month") int month);

    // Search members by name, phone, or email
    @Query("SELECT m FROM Member m WHERE m.gym.id = :gymId AND (" +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "m.phone LIKE CONCAT('%', :query, '%') OR " +
            "LOWER(m.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Member> searchMembersByGym(@Param("gymId") Long gymId, @Param("query") String query);

    // Filter with optional parameters
    @Query("SELECT m FROM Member m WHERE m.gym.id = :gymId " +
            "AND (:status IS NULL OR m.status = :status) " +
            "AND (:planName IS NULL OR m.currentPlan.name = :planName) " +
            "AND (:search IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR m.phone LIKE CONCAT('%', :search, '%'))")
    Page<Member> findWithFilters(
            @Param("gymId") Long gymId,
            @Param("status") String status,
            @Param("search") String search,
            @Param("planName") String planName,
            Pageable pageable
    );

    @Query("SELECT new com.gymflow.gymflow.dashboard.dto.PopularPlanDTO(p.name, COUNT(m)) " +
            "FROM Member m JOIN m.currentPlan p " +
            "WHERE m.gym.id = :gymId " +
            "GROUP BY p.name " +
            "ORDER BY COUNT(m) DESC")
    Page<PopularPlanDTO> findPopularPlans(@Param("gymId") Long gymId, Pageable pageable);

}