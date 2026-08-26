package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import java.math.BigDecimal;
import java.time.Instant;

import com.room_rental_backend.room_rental_application.enums.RoommateRequestStatus;

// New API: roommate request as seen by requester or recipient.
public record RoommateRequestResponse(
        String id,
        String roomId,
        String roomTitle,
        String propertyName,
        BigDecimal roomPrice,
        UserSummary requester,
        UserSummary recipient,
        RoommateRequestStatus status,
        String message,
        Instant createdAt,
        Instant updatedAt) {

    // Minimal identity of the other tenant; no emails or phone numbers.
    public record UserSummary(String userId, String name, String profilePictureUrl) {
    }
}
