package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import java.math.BigDecimal;

import com.room_rental_backend.room_rental_application.enums.Cleanliness;
import com.room_rental_backend.room_rental_application.enums.SleepSchedule;

// New API: a tenant's roommate profile as seen by others. Deliberately excludes
// private data (email, phone); only display name and avatar are shared.
public record RoommateProfileResponse(
        String profileId,
        String userId,
        String name,
        String profilePictureUrl,
        String bio,
        BigDecimal budget,
        String preferredLocation,
        String preferredMoveIn,
        Boolean smoker,
        Boolean petsOk,
        SleepSchedule sleepSchedule,
        Cleanliness cleanliness) {

    public static RoommateProfileResponse empty(String userId, String name, String profilePictureUrl) {
        return new RoommateProfileResponse(null, userId, name, profilePictureUrl, null, null, null, null, null, null,
                null, null);
    }
}
