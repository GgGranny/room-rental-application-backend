package com.room_rental_backend.room_rental_application.models;

import java.time.Instant;
import java.time.LocalDateTime;

import com.room_rental_backend.room_rental_application.enums.NotificationType;
import com.room_rental_backend.room_rental_application.models.base_entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Stores the in-app history for notifications that are also delivered through FCM.
@Entity
@Table(name = "user_notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotification extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, length = 1000)
    private String body;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    private String referenceId;
    @Builder.Default
    @Column(nullable = false)
    private boolean read = false;
    @Builder.Default
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
