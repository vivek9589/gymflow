package com.gymflow.gymflow.auth.entity;

import com.gymflow.gymflow.auth.enums.Role;
import com.gymflow.gymflow.gym.entity.Gym;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Represents a Gym Owner in the system.
 * Each owner is linked to a single Gym.
 */
@Entity
@Table(name = "gym_owners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymOwner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ownerName;

    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Password is stored in BCrypt hashed format.
     * Never expose this field in API responses.
     */
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.OWNER;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "gym_id", referencedColumnName = "id")
    private Gym gym;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}