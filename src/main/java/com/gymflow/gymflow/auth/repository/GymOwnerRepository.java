package com.gymflow.gymflow.auth.repository;


import com.gymflow.gymflow.auth.entity.GymOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GymOwnerRepository extends JpaRepository<GymOwner, Long> {

    // Industry Standard: Use Optional to avoid NullPointerExceptions in your Service layer
    Optional<GymOwner> findByEmail(String email);

    // Check if email already exists during registration
    Boolean existsByEmail(String email);
}