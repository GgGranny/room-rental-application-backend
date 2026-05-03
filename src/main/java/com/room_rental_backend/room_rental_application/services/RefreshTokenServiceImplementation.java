package com.room_rental_backend.room_rental_application.services;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.room_rental_backend.room_rental_application.exceptions.TokenExpiredException;
import com.room_rental_backend.room_rental_application.exceptions.TokenNotFoundException;
import com.room_rental_backend.room_rental_application.exceptions.UserNotFoundException;
import com.room_rental_backend.room_rental_application.interfaces.RefreshTokenService;
import com.room_rental_backend.room_rental_application.models.RefreshToken;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.repositories.RefreshTokenRepository;
import com.room_rental_backend.room_rental_application.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RefreshTokenServiceImplementation implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${refresh.token.expiration}")
    private long refreshTokenExpiration;

    @Override
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RefreshToken createRefreshToken(Users user) {

        if (user == null) {
            throw new UserNotFoundException(
                    "Cannot create refresh token — user is null");
        }

        // Delete old token by user ID (plain String) — avoids any detached-entity
        // issues.
        // The caller's TX is suspended, so we need no reference to its cached objects.
        refreshTokenRepository.deleteByUserId(user.getId());

        // Re-fetch the user as a fully managed entity inside this fresh transaction
        Users managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found: " + user.getId()));

        RefreshToken refreshToken = RefreshToken.builder()
                .token(generateRefreshToken())
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiration * 1000))
                .user(managedUser)
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);

        log.debug("Refresh token created for user: {} expires at: {}",
                managedUser.getEmail(), saved.getExpiresAt());

        return saved;
    }

    @Override
    public RefreshToken validateToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException(
                        "Refresh token not found: " + token));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenExpiredException(
                    "Refresh token expired. Please login again.");
        }

        log.debug("Refresh token valid for user: {}",
                refreshToken.getUser().getEmail());

        return refreshToken;
    }

    @Override
    public boolean isRefreshTokenExpired(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(rt -> rt.getExpiresAt().isBefore(Instant.now()))
                .orElseThrow(() -> new TokenNotFoundException(
                        "Refresh token not found: " + token));
    }

    @Override
    @Transactional
    public void deleteToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(rt -> {
                    refreshTokenRepository.delete(rt);
                    log.debug("Refresh token deleted for user: {}",
                            rt.getUser().getEmail());
                });
    }

    @Override
    @Transactional
    public void deleteAllTokensForUser(Users user) {
        refreshTokenRepository.deleteByUserId(user.getId());
        log.debug("All refresh tokens deleted for user: {}", user.getEmail());
    }
}