package com.room_rental_backend.room_rental_application.dtos.requestDtos;

import java.math.BigDecimal;

import com.room_rental_backend.room_rental_application.enums.Cleanliness;
import com.room_rental_backend.room_rental_application.enums.SleepSchedule;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

// New API: create/update payload for the authenticated tenant's roommate profile.
public record RoommateProfileRequest(
        @Size(max = 500) String bio,
        @DecimalMin("0.0") BigDecimal budget,
        @Size(max = 120) String preferredLocation,
        @Size(max = 50) String preferredMoveIn,
        Boolean smoker,
        Boolean petsOk,
        SleepSchedule sleepSchedule,
        Cleanliness cleanliness) {
}
