package com.gymflow.gymflow.gym.repository;



import com.gymflow.gymflow.gym.entity.Gym;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GymRepository extends JpaRepository<Gym, Long> {
    Optional<Gym> findByGymCode(String gymCode);
    boolean existsByGymCode(String gymCode);
}