package com.room_rental_backend.room_rental_application.interfaces;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.CompleteUserProfileRequest;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.RefreshTokenRequest;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.RegisterUserRequestDtos;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.UserLoginRequestDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterUserRequestDtos request);

    AuthResponse login(UserLoginRequestDto request);

    AuthResponse activateUser(String userId);

    AuthResponse refreshToken(RefreshTokenRequest refreshToken);

    AuthResponse completeUserProfile(CompleteUserProfileRequest request);

    boolean isProfileCompleted();
}
