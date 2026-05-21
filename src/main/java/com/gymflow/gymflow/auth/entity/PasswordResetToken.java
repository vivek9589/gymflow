package com.gymflow.gymflow.auth.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private GymOwner gymOwner;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    public PasswordResetToken(String token, GymOwner gymOwner, int expiryInMinutes) {
        this.token = token;
        this.gymOwner = gymOwner;
        this.expiryDate = LocalDateTime.now().plusMinutes(expiryInMinutes);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}