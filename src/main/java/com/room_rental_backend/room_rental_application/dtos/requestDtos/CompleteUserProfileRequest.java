package com.room_rental_backend.room_rental_application.dtos.requestDtos;

import com.room_rental_backend.room_rental_application.enums.Roles;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public record CompleteUserProfileRequest(
                String userId,
                @NotBlank(message = "fname is required") String fname,
                @NotBlank(message = "lname is required") String lname,
                @NotNull(message = "dob is required") String dob,
                @NotNull(message = "role is not provided") Roles role) {
}
