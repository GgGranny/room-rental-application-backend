package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import java.time.Instant;

import com.room_rental_backend.room_rental_application.enums.MatchStatus;

// New API: a roommate match as seen by one of its two tenants. Exposes only the
// OTHER tenant's minimal identity (peer) plus the conversation handle and unread
// count. No emails, phone numbers, KYC data or private details are included.
public record RoommateMatchResponse(
        String matchId,
        String conversationId,
        String roomId,
        String roomTitle,
        MatchStatus status,
        UserSummary peer,
        long unreadCount,
        Instant createdAt) {

    // Minimal identity of the other tenant; no emails or phone numbers.
    public record UserSummary(String userId, String name, String profilePictureUrl) {
    }
}
