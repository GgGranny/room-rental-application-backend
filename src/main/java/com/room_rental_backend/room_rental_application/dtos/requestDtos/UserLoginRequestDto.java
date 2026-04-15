package com.room_rental_backend.room_rental_application.dtos.requestDtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UserLoginRequestDto(
        @Email(message = "Invalid email format") @NotEmpty(message = "Email cannot be empty") String email,

        @NotEmpty(message = "Password cannot be empty") @Size(min = 8, message = "Password must be at least 8 characters long") String password) {
}
