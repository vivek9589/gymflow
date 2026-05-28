package com.gymflow.gymflow.payment.repository;

import com.gymflow.gymflow.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByMemberId(Long memberId);
    List<Payment> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    // INDUSTRY STANDARD: Aggregate actual revenue directly from the transactional ledger within a timeframe
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.gymId = :gymId " +
            "AND p.status = 'SUCCESS' " +
            "AND p.createdAt >= :start " +
            "AND p.createdAt <= :end")
    BigDecimal calculateRevenueForPeriod(
            @Param("gymId") Long gymId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}