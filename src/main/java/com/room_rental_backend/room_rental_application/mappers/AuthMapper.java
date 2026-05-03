package com.room_rental_backend.room_rental_application.mappers;

import java.lang.ProcessBuilder.Redirect;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.RegisterUserRequestDtos;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.AuthResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RedirectUrl;
import com.room_rental_backend.room_rental_application.enums.Roles;
import com.room_rental_backend.room_rental_application.models.Users;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthMapper {

    private final PasswordEncoder passwordEncoder;

    public AuthResponse toAuthResponse(Users user, String token, String refreshToken) {
        AuthResponse response = new AuthResponse(
                user.getId(),
                token,
                refreshToken,
                user.getRoles(),
                user.getFname(),
                user.getLname(),
                user.getEmail(),
                user.getDateOfBirth(),
                user.isVerified());
        return response;
    }

    public Users toUsers(RegisterUserRequestDtos requestDtos) {
        Users user = new Users();
        user.setEmail(requestDtos.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(requestDtos.password()));
        return user;
    }

    public RedirectUrl toRedirectUrl(String pageName, String url, HttpStatus code) {
        return RedirectUrl.builder()
                .code(code)
                .url(url)
                .pageName(pageName)
                .build();
    }
}
