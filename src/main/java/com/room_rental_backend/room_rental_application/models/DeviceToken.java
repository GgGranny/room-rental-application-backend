package com.room_rental_backend.room_rental_application.models;

import com.room_rental_backend.room_rental_application.models.base_entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// New API: one row per FCM device/browser token registered by an authenticated
// user. A user may own many tokens (multiple browsers/devices); only active
// tokens receive push notifications.
@Entity
@Table(name = "device_tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeviceToken extends BaseEntity {

    // The user that owns this registration. Never derived from client input.
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    // FCM marks tokens as invalid/unregistered over time; we flip this flag
    // instead of deleting history so stale registrations never accumulate.
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}