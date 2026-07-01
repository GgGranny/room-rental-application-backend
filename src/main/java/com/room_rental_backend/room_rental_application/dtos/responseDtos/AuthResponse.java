package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import com.room_rental_backend.room_rental_application.enums.Roles;

import lombok.Builder;

@Builder
public record AuthResponse(
                String userId,
                String landlordId,
                String token,
                String refreshToken,
                Roles role,
                String fname,
                String lname,
                String email,
                String Dob,
                boolean isVerifird) {
}