package com.gymflow.gymflow.auth.repository;


import com.gymflow.gymflow.auth.entity.GymOwner;
import com.gymflow.gymflow.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GymOwnerRepository extends JpaRepository<GymOwner, Long> {

    // Industry Standard: Use Optional to avoid NullPointerExceptions in your Service layer
    Optional<GymOwner> findByEmail(String email);


    @Query("SELECT o FROM GymOwner o JOIN FETCH o.gym WHERE o.email = :email")
    Optional<GymOwner> findByEmailWithGym(@Param("email") String email);



    // Check if email already exists during registration
    Boolean existsByEmail(String email);
}