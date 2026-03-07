package com.gymflow.gymflow.auth.entity;

import com.gymflow.gymflow.auth.enums.Role;
import com.gymflow.gymflow.gym.entity.Gym;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Entity
@Table(name = "gym_owners")
@Data
public class GymOwner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Is line ko add karein 👇
    @Column(nullable = false)
    private String ownerName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // BCrypt Hashed

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.OWNER;

    @OneToOne
    @JoinColumn(name = "gym_id")
    private Gym gym;

    private LocalDateTime createdAt = LocalDateTime.now();
}