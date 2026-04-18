package com.room_rental_backend.room_rental_application.services;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.room_rental_backend.room_rental_application.exceptions.TokenExpiredException;
import com.room_rental_backend.room_rental_application.exceptions.TokenNotFoundException;
import com.room_rental_backend.room_rental_application.exceptions.UserNotFoundException;
import com.room_rental_backend.room_rental_application.interfaces.RefreshTokenService;
import com.room_rental_backend.room_rental_application.models.RefreshToken;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.repositories.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImplementation implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${refresh.token.expiration}")
    private long refreshTokenExpiration;

    @Override
    public String generateRefreshToken() {
        String randomString = UUID.randomUUID().toString();
        return randomString;
    }

    @Override
    public boolean isRefreshTokenExpired(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(
                        () -> new TokenNotFoundException("The Refersh Token " + refreshToken + " Does not Exists"));
        return token.getExpiresAt().isBefore(Instant.now());
    }

    @Override
    public RefreshToken createRefreshToken(Users user) {
        if (user == null) {
            throw new UserNotFoundException("User is null to save the refresh token");
        }

        refreshTokenRepository.deleteByUser(user);
        RefreshToken refreshToken = RefreshToken.builder()
                .token(generateRefreshToken())
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiration))
                .user(user)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken validateToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("The Refresh Token " + token + " Does not Exists"));

        if (isRefreshTokenExpired(token)) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenExpiredException("Refresh token is expired");
        }
        return refreshToken;
    }

}
