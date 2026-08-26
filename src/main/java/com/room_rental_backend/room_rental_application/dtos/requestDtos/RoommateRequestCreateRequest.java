package com.room_rental_backend.room_rental_application.dtos.requestDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// New API: send a roommate request for a shared room. The requester is always
// resolved from the JWT — the client never sends a requester id.
public record RoommateRequestCreateRequest(
        @NotBlank String recipientId,
        @NotBlank String roomId,
        @Size(max = 300) String message) {
}
