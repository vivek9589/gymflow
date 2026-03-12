package com.gymflow.gymflow.member.entity;

import com.gymflow.gymflow.gym.entity.Gym;
import com.gymflow.gymflow.plan.entity.Plan;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a Gym Member entity with personal details,
 * subscription information, and links to Gym and Plan.
 */
@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    private String email;
    private String bloodGroup;
    private Double weight;
    private Double height;

    private LocalDate dob;
    private String occupation;
    private String fatherName;
    private String permanentAddress;

    @Column(columnDefinition = "TEXT")
    private String medicalConditions;

    private Double initialPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDate registrationDate;

    @Column(nullable = false)
    private String status = "PENDING"; // Default status

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan currentPlan;

    private LocalDate subscriptionStartDate;
    private LocalDate expiryDate;

    private boolean whatsappEnabled = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}