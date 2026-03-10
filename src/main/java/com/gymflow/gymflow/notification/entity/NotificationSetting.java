package com.gymflow.gymflow.notification.entity;

import com.gymflow.gymflow.gym.entity.Gym;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "notification_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "gym_id")
    private Gym gym;

    private boolean welcomeEnabled;

    private boolean expiryEnabled;

    private boolean promotionEnabled;

    private boolean holidayEnabled;
}