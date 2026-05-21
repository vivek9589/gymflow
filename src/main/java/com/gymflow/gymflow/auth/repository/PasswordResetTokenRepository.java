package com.gymflow.gymflow.auth.repository;

import com.gymflow.gymflow.auth.entity.PasswordResetToken;
import com.gymflow.gymflow.auth.entity.GymOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByGymOwner(GymOwner gymOwner);
}