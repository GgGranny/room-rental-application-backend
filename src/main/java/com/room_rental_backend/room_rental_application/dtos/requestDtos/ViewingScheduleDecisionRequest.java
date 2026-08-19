package com.room_rental_backend.room_rental_application.dtos.requestDtos;

import jakarta.validation.constraints.NotBlank;

// New API: landlord's decision on a viewing request. status is APPROVED or
// REJECTED; responseNote is an optional message shown to the tenant.
public record ViewingScheduleDecisionRequest(
                @NotBlank(message = "status is required") String status,
                String responseNote) {
}
