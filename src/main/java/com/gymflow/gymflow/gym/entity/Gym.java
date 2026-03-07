package com.gymflow.gymflow.gym.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;



@Entity
@Table(name = "gyms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Gym {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String gymCode;

    private String address;
    private String contactNumber;
    private String city;
    private String state;
    private String pincode;
    private String website;
    private Integer establishedYear;
    private String logoUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime createdAt = LocalDateTime.now();
}