package com.room_rental_backend.room_rental_application.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.RegisterTokenRequest;
import com.room_rental_backend.room_rental_application.exceptions.UnauthorizedException;
import com.room_rental_backend.room_rental_application.exceptions.UserNotFoundException;
import com.room_rental_backend.room_rental_application.interfaces.NotificationService;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.repositories.UserRepository;
import com.room_rental_backend.room_rental_application.responseHandler.ApiResponse;
import com.room_rental_backend.room_rental_application.responseHandler.GlobalResponseHandler;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// New API: FCM token registration for push notifications. The authenticated
// principal (JWT email) is resolved server-side on every call — the client can
// never register/remove a token for another user. Matched by SpringSecurity's
// "anyRequest().authenticated()" rule so every role may register its own token.
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // Register or refresh the current user's device/browser token.
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<Void>> registerToken(
            @Valid @RequestBody RegisterTokenRequest request,
            Authentication authentication) {
        Users user = resolveUser(authentication);
        notificationService.registerToken(user, request.token());
        return GlobalResponseHandler.success("Notification token registered successfully", null, HttpStatus.OK);
    }

    // Remove the current user's token (or all of their tokens when no token is sent).
    @DeleteMapping("/token")
    public ResponseEntity<ApiResponse<Void>> removeToken(
            @RequestBody(required = false) RegisterTokenRequest request,
            Authentication authentication) {
        Users user = resolveUser(authentication);
        if (request != null && request.token() != null && !request.token().isBlank()) {
            notificationService.unregisterToken(user, request.token());
        } else {
            notificationService.unregisterAllTokens(user);
        }
        return GlobalResponseHandler.success("Notification token removed successfully", null, HttpStatus.OK);
    }

    private Users resolveUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Not authenticated");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + authentication.getName()));
    }
}