package com.room_rental_backend.room_rental_application.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.RegisterUserRequestDtos;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.UserLoginRequestDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.AuthResponse;
import com.room_rental_backend.room_rental_application.interfaces.AuthService;
import com.room_rental_backend.room_rental_application.responseHandler.ApiResponse;
import com.room_rental_backend.room_rental_application.responseHandler.GlobalResponseHandler;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth/")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody UserLoginRequestDto entity) {
        AuthResponse response = authService.login(entity);
        return GlobalResponseHandler.success(
                "Login Successful",
                response,
                HttpStatus.OK);
    }

    @PostMapping("register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterUserRequestDtos registerUserRequestDtos) {
        AuthResponse response = authService.register(registerUserRequestDtos);
        return GlobalResponseHandler.success(
                "Registration Successful",
                response,
                HttpStatus.OK);
    }

}
