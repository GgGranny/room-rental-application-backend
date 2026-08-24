package com.room_rental_backend.room_rental_application.interfaces;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.UpdateProfileRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.UserResponse;

// New API: self-service profile management for the authenticated user.
public interface UserProfileService {
    UserResponse getMyProfile(Authentication authentication);

    UserResponse updateMyProfile(UpdateProfileRequest request, Authentication authentication);

    UserResponse updateMyAvatar(MultipartFile file, Authentication authentication);
}
