package com.room_rental_backend.room_rental_application.interfaces;

import com.room_rental_backend.room_rental_application.models.RefreshToken;
import com.room_rental_backend.room_rental_application.models.Users;

public interface RefreshTokenService {

    String generateRefreshToken();

    boolean isRefreshTokenExpired(String refreshToken);

    RefreshToken createRefreshToken(Users activatedUser);

    RefreshToken validateToken(String token);
}
