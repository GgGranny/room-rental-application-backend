package com.room_rental_backend.room_rental_application.models;

import java.math.BigDecimal;

import com.room_rental_backend.room_rental_application.enums.Cleanliness;
import com.room_rental_backend.room_rental_application.enums.SleepSchedule;
import com.room_rental_backend.room_rental_application.models.base_entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// New API: a tenant's roommate-seeking profile (preferences + short bio).
// One profile per user; reuses the existing Users identity, no duplicated user data.
@Entity
@Table(name = "roommate_profiles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoommateProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Users user;

    @Column(name = "bio", length = 500)
    private String bio;

    // Monthly budget the tenant is comfortable contributing.
    @Column(name = "budget", precision = 12, scale = 2)
    private BigDecimal budget;

    // Preferred area/city in Nepal (free text, non-sensitive).
    @Column(name = "preferred_location", length = 120)
    private String preferredLocation;

    // Preferred move-in period as flexible text (e.g. "September 2026", "ASAP").
    @Column(name = "preferred_move_in", length = 50)
    private String preferredMoveIn;

    @Column(name = "smoker")
    private Boolean smoker;

    @Column(name = "pets_ok")
    private Boolean petsOk;

    @Enumerated(EnumType.STRING)
    @Column(name = "sleep_schedule", length = 20)
    private SleepSchedule sleepSchedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "cleanliness", length = 20)
    private Cleanliness cleanliness;
}
