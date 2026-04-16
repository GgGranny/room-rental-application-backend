package com.room_rental_backend.room_rental_application.mappers;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.RegisterUserRequestDtos;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.AuthResponse;
import com.room_rental_backend.room_rental_application.models.Users;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthMapper {

    private final PasswordEncoder passwordEncoder;

    public AuthResponse toAuthResponse(Users user, String token) {
        AuthResponse response = new AuthResponse(
                user.getId(),
                token,
                user.getRoles(),
                user.getFname(),
                user.getLname(),
                user.getEmail(),
                user.getDateOfBirth());
        return response;
    }

    public Users toUsers(RegisterUserRequestDtos requestDtos) {
        Users user = new Users();
        user.setFname(requestDtos.fname());
        user.setLname(requestDtos.lname());
        user.setEmail(requestDtos.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(requestDtos.password()));
        user.setRoles(requestDtos.role());
        user.setDateOfBirth(requestDtos.dob());
        user.setPhoneNumber(requestDtos.phoneNumber());
        return user;
    }
}
