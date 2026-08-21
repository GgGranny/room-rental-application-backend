package com.room_rental_backend.room_rental_application.dtos.requestDtos;

import jakarta.validation.constraints.NotBlank;

// New API: payload for registering/removing the authenticated user's FCM token.
// No user id is accepted — ownership always comes from the JWT principal.
public record RegisterTokenRequest(
                @NotBlank(message = "token is required") String token) {
}