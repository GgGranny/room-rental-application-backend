package com.room_rental_backend.room_rental_application.dtos.responseDtos;

// New API: a candidate roommate discovered from a shared room's interest list,
// with the simple (non-AI) preference compatibility score computed by the backend.
public record RoommateCandidateResponse(
        RoommateProfileResponse profile,
        int compatibilityScore) {
}
