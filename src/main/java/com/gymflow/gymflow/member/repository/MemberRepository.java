package com.gymflow.gymflow.member.repository;



import com.gymflow.gymflow.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByGymId(Long gymId);
    List<Member> findByGymIdAndStatus(Long gymId, String status);

    long countByGymId(Long gymId);
    long countByGymIdAndStatus(Long gymId, String status);

    List<Member> findTop5ByGymIdOrderByCreatedAtDesc(Long gymId);

    @Query("SELECT SUM(m.currentPlan.price) FROM Member m WHERE m.gym.id = :gymId AND MONTH(m.registrationDate) = :month")
    BigDecimal calculateMonthlyRevenue(Long gymId, int month);

    @Query("SELECT m FROM Member m WHERE m.gym.id = :gymId AND (" +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "m.phone LIKE CONCAT('%', :query, '%') OR " +
            "LOWER(m.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Member> searchMembersByGym(@Param("gymId") Long gymId, @Param("query") String query);


}