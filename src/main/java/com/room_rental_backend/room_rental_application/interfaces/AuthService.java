package com.room_rental_backend.room_rental_application.interfaces;

import java.util.Map;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.RegisterUserRequestDtos;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.UserLoginRequestDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterUserRequestDtos request);

    AuthResponse login(UserLoginRequestDto request);
}
