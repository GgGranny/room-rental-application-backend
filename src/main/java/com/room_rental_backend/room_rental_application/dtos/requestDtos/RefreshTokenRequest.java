package com.room_rental_backend.room_rental_application.dtos.requestDtos;

import jakarta.validation.constraints.NotNull;

public record RefreshTokenRequest(@NotNull(message = "refresh token is required") String refreshToken) {
}
