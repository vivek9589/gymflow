package com.gymflow.gymflow.plan.entity;

import com.gymflow.gymflow.gym.entity.Gym;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a subscription plan offered by a gym.
 */
@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // e.g., "Premium 3 Months"

    @Column(nullable = false)
    private BigDecimal price; // Use BigDecimal for monetary values

    @Column(nullable = false)
    private Integer durationInDays; // e.g., 30, 90, 365

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym;

    private boolean isActive = true; // Soft delete flag

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}