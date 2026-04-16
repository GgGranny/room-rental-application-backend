package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import com.room_rental_backend.room_rental_application.enums.Roles;

public record AuthResponse(String userId, String token, Roles role, String fname, String lname, String email,
        String Dob) {
}