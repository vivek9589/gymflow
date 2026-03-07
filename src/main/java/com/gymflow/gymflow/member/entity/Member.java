package com.gymflow.gymflow.member.entity;


import com.gymflow.gymflow.gym.entity.Gym;
import com.gymflow.gymflow.plan.entity.Plan;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // [cite: 5]

    @Column(nullable = false)
    private String phone; // [cite: 13]

    private String email; // [cite: 15]
    private String bloodGroup; // [cite: 18]
    private Double weight; // [cite: 22]
    private Double height; // [cite: 21]

    // New fields from the Admission Form
    private LocalDate dob; // [cite: 7]
    private String occupation; // [cite: 9]
    private String fatherName; // [cite: 11]
    private String permanentAddress; // [cite: 17]

    @Column(columnDefinition = "TEXT")
    private String medicalConditions; // [cite: 29]

    private Double initialPayment; // [cite: 26]

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym;

    private LocalDate registrationDate = LocalDate.now();

    @Column(nullable = false)
    private String status = "PENDING"; // Changed default to PENDING for QR joins

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan currentPlan; // [cite: 24]

    private LocalDate subscriptionStartDate;
    private LocalDate expiryDate;

    private LocalDateTime createdAt = LocalDateTime.now();
}