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
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // --- Basic Finders (Filtered) ---

    // Updated from findByGymId
    List<Member> findByGymIdAndDeletedFalse(Long gymId);

    // Updated counts to exclude deleted members
    long countByGymIdAndDeletedFalse(Long gymId);

    long countByGymIdAndStatusAndDeletedFalse(Long gymId, String status);

    // Dashboard Recent Members (Filtered)
    List<Member> findTop5ByGymIdAndDeletedFalseOrderByCreatedAtDesc(Long gymId);


    // --- Subscription & Expiry (Filtered) ---

    List<Member> findByExpiryDateAndDeletedFalse(LocalDate expiryDate);

    List<Member> findByGymIdAndExpiryDateBetweenAndDeletedFalse(Long gymId, LocalDate start, LocalDate end);


    // --- Revenue Calculation ---
    // NOTE: We usually keep deleted members in revenue calculations
    // because money already paid still contributes to financial history.
    @Query("SELECT COALESCE(SUM(m.initialPayment), 0) " +
            "FROM Member m " +
            "WHERE m.gym.id = :gymId AND FUNCTION('MONTH', m.registrationDate) = :month")
    BigDecimal calculateMonthlyRevenue(@Param("gymId") Long gymId, @Param("month") int month);


    // --- Search & Filters (Filtered) ---

    @Query("SELECT m FROM Member m WHERE m.gym.id = :gymId AND m.deleted = false AND (" +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "m.phone LIKE CONCAT('%', :query, '%') OR " +
            "LOWER(m.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Member> searchMembersByGym(@Param("gymId") Long gymId, @Param("query") String query);

    @Query("SELECT m FROM Member m WHERE m.gym.id = :gymId " +
            "AND m.deleted = false " +
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


    // --- Analytics (Filtered) ---

    @Query("SELECT new com.gymflow.gymflow.dashboard.dto.PopularPlanDTO(p.name, COUNT(m)) " +
            "FROM Member m JOIN m.currentPlan p " +
            "WHERE m.gym.id = :gymId AND m.deleted = false " +
            "GROUP BY p.name " +
            "ORDER BY COUNT(m) DESC")
    Page<PopularPlanDTO> findPopularPlans(@Param("gymId") Long gymId, Pageable pageable);


    // --- Utility Finders ---

    List<Member> findAllByDeletedFalse();

    Optional<Member> findByIdAndDeletedFalse(Long id);
}