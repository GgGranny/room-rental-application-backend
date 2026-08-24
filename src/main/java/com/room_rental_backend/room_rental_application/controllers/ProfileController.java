package com.room_rental_backend.room_rental_application.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.UpdateProfileRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.UserResponse;
import com.room_rental_backend.room_rental_application.exceptions.UnauthorizedException;
import com.room_rental_backend.room_rental_application.interfaces.UserProfileService;
import com.room_rental_backend.room_rental_application.responseHandler.ApiResponse;
import com.room_rental_backend.room_rental_application.responseHandler.GlobalResponseHandler;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// New API: authenticated, self-service profile endpoints for every role
// (tenant/landlord/admin). The authenticated principal is resolved server-side,
// so no endpoint ever accepts a user id from the client. Matched by
// SpringSecurity's "anyRequest().authenticated()" rule.
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileService userProfileService;

    // Current user's own full profile (name, contact info, avatar, KYC flags).
    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(Authentication authentication) {
        requireAuthentication(authentication);
        return GlobalResponseHandler.success("Profile fetched successfully",
                userProfileService.getMyProfile(authentication), HttpStatus.OK);
    }

    // Update editable fields of the current user's own profile.
    @PutMapping
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        requireAuthentication(authentication);
        return GlobalResponseHandler.success("Profile updated successfully",
                userProfileService.updateMyProfile(request, authentication), HttpStatus.OK);
    }

    // Upload/replace the current user's own profile picture.
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> updateMyAvatar(
            @RequestPart("file") MultipartFile file,
            Authentication authentication) {
        requireAuthentication(authentication);
        return GlobalResponseHandler.success("Profile picture updated successfully",
                userProfileService.updateMyAvatar(file, authentication), HttpStatus.OK);
    }

    private void requireAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Not authenticated");
        }
    }
}
