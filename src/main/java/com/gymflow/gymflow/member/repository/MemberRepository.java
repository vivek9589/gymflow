package com.gymflow.gymflow.member.repository;



import com.gymflow.gymflow.dashboard.dto.PopularPlanDTO;
import com.gymflow.gymflow.member.dto.response.MemberListResponseDto;
import com.gymflow.gymflow.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface MemberRepository extends
        JpaRepository<Member, Long>,
        JpaSpecificationExecutor<Member> {

    List<Member> findByGymIdAndDeletedFalse(Long gymId);

    long countByGymIdAndDeletedFalse(Long gymId);

    long countByGymIdAndStatusAndDeletedFalse(Long gymId, String status);

    List<Member> findTop5ByGymIdAndDeletedFalseOrderByCreatedAtDesc(Long gymId);

    List<Member> findByExpiryDateAndDeletedFalse(LocalDate expiryDate);

    List<Member> findByGymIdAndExpiryDateBetweenAndDeletedFalse(
            Long gymId,
            LocalDate start,
            LocalDate end
    );

    Optional<Member> findByCheckInToken(String checkInToken);

    @Query("""
            SELECT COALESCE(SUM(m.initialPayment),0)
            FROM Member m
            WHERE m.gym.id=:gymId
            AND FUNCTION('MONTH',m.registrationDate)=:month
            """)
    BigDecimal calculateMonthlyRevenue(
            @Param("gymId") Long gymId,
            @Param("month") int month
    );

    @Query("""
            SELECT m
            FROM Member m
            WHERE m.gym.id=:gymId
            AND m.deleted=false
            AND(
                LOWER(m.name) LIKE LOWER(CONCAT('%',:query,'%'))
                OR m.phone LIKE CONCAT('%',:query,'%')
                OR LOWER(m.email) LIKE LOWER(CONCAT('%',:query,'%'))
            )
            """)
    List<Member> searchMembersByGym(
            @Param("gymId") Long gymId,
            @Param("query") String query
    );

    @Query("""
            SELECT COUNT(m)
            FROM Member m
            WHERE m.gym.id=:gymId
            AND m.deleted=false
            """)
    long countTotalMembers(Long gymId);

    @Query("""
            SELECT COUNT(m)
            FROM Member m
            WHERE m.gym.id=:gymId
            AND m.deleted=false
            AND m.status='ACTIVE'
            AND m.expiryDate>=:today
            """)
    long countActiveMembers(
            Long gymId,
            LocalDate today
    );

    @Query("""
            SELECT COUNT(m)
            FROM Member m
            WHERE m.gym.id=:gymId
            AND m.deleted=false
            AND m.expiryDate<:today
            """)
    long countExpiredMembers(
            Long gymId,
            LocalDate today
    );

    @Query("""
            SELECT new com.gymflow.gymflow.dashboard.dto.PopularPlanDTO(
                p.name,
                COUNT(m)
            )
            FROM Member m
            JOIN m.currentPlan p
            WHERE m.gym.id=:gymId
            AND m.deleted=false
            GROUP BY p.name
            ORDER BY COUNT(m) DESC
            """)
    Page<PopularPlanDTO> findPopularPlans(
            @Param("gymId") Long gymId,
            Pageable pageable
    );

    List<Member> findAllByDeletedFalse();

    Optional<Member> findByIdAndDeletedFalse(Long id);

}