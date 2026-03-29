package com.gymflow.gymflow.payment.repository;

import com.gymflow.gymflow.payment.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findTopByMemberIdOrderByCreatedAtDesc(Long memberId);
}
