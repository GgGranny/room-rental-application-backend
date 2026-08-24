package com.room_rental_backend.room_rental_application.dtos.requestDtos;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// New API: editable profile fields for the authenticated user's own account.
// Email/role are intentionally absent — they are never self-editable.
public record UpdateProfileRequest(
                @Size(max = 50, message = "First name must not exceed 50 characters") String fname,
                @Size(max = 50, message = "Last name must not exceed 50 characters") String lname,
                @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits") String phoneNumber,
                String dateOfBirth) {
}
