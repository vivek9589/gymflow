package com.gymflow.gymflow.plan.entity;


import com.gymflow.gymflow.gym.entity.Gym;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // e.g., "Premium 3 Months"

    @Column(nullable = false)
    private BigDecimal price; // Industry standard: Use BigDecimal for money

    @Column(nullable = false)
    private Integer durationInDays; // e.g., 30, 90, 365

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym;

    private boolean isActive = true;
}