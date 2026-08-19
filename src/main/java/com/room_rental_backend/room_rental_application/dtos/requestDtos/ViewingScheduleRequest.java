package com.room_rental_backend.room_rental_application.dtos.requestDtos;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// New API: payload a tenant sends to request a room viewing.
public record ViewingScheduleRequest(
                @NotBlank(message = "roomId is required") String roomId,
                @NotNull(message = "scheduledAt is required") @Future(message = "scheduledAt must be in the future") LocalDateTime scheduledAt,
                String note) {
}
