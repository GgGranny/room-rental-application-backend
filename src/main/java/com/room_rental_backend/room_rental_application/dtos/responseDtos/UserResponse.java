package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import com.room_rental_backend.room_rental_application.enums.KycStatus;
import com.room_rental_backend.room_rental_application.enums.Roles;

import lombok.Builder;

@Builder
public record UserResponse(
                String userId,
                String email,
                String phoneNumber,
                Roles role,
                String fname,
                String lname,
                String dateOfBirth,
                String profilePictureUrl,
                String provider,
                boolean verified,
                boolean active,
                boolean profileCompleted,
                boolean kycSubmitted,
                KycStatus kycStatus) {
}
