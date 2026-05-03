package com.room_rental_backend.room_rental_application.interfaces;

import com.room_rental_backend.room_rental_application.models.RefreshToken;
import com.room_rental_backend.room_rental_application.models.Users;

public interface RefreshTokenService {

    String generateRefreshToken();

    RefreshToken createRefreshToken(Users user);

    RefreshToken validateToken(String token);

    boolean isRefreshTokenExpired(String token);

    void deleteToken(String token);

    void deleteAllTokensForUser(Users user);
}
