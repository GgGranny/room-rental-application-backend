package com.room_rental_backend.room_rental_application.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.UpdateProfileRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.UserResponse;
import com.room_rental_backend.room_rental_application.exceptions.PhoneNumberAlreadyExists;
import com.room_rental_backend.room_rental_application.exceptions.UnauthorizedException;
import com.room_rental_backend.room_rental_application.exceptions.UserNotFoundException;
import com.room_rental_backend.room_rental_application.interfaces.SupabaseFileStorageService;
import com.room_rental_backend.room_rental_application.interfaces.UserProfileService;
import com.room_rental_backend.room_rental_application.mappers.UserMapper;
import com.room_rental_backend.room_rental_application.models.ImageMetadata;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

// New API: implements self-service profile management. The profile owner is
// always resolved from the authenticated principal (JWT email) — a client can
// never read or update another user's profile.
@Service
@RequiredArgsConstructor
public class UserProfileServiceImplementation implements UserProfileService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SupabaseFileStorageService supabaseFileStorageService;

    @Override
    public UserResponse getMyProfile(Authentication authentication) {
        return userMapper.toDto(resolveUser(authentication));
    }

    @Transactional
    @Override
    public UserResponse updateMyProfile(UpdateProfileRequest request, Authentication authentication) {
        Users user = resolveUser(authentication);

        if (request.fname() != null) {
            user.setFname(request.fname().isBlank() ? null : request.fname().trim());
        }
        if (request.lname() != null) {
            user.setLname(request.lname().isBlank() ? null : request.lname().trim());
        }
        if (request.dateOfBirth() != null) {
            user.setDateOfBirth(request.dateOfBirth().isBlank() ? null : request.dateOfBirth().trim());
        }
        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            String phone = request.phoneNumber().trim();
            // Phone numbers are unique per users table — reject duplicates that are not
            // the user's own current number.
            boolean unchanged = phone.equals(user.getPhoneNumber());
            if (!unchanged && userRepository.existsByPhoneNumber(phone)) {
                throw new PhoneNumberAlreadyExists("Phone number already exists");
            }
            user.setPhoneNumber(phone);
        }

        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    @Override
    public UserResponse updateMyAvatar(MultipartFile file, Authentication authentication) {
        Users user = resolveUser(authentication);
        // Reuses the existing Supabase storage pipeline (public bucket, PROFILE type).
        ImageMetadata metadata = supabaseFileStorageService.uploadFile(file, "profile", "public", "PROFILE");
        user.setProfilePictureUrl(metadata.getUrl());
        return userMapper.toDto(userRepository.save(user));
    }

    private Users resolveUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Not authenticated");
        }
        Object principal = authentication.getPrincipal();
        String email;
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else if (principal instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
        } else {
            email = authentication.getName();
        }
        if (email == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));
    }
}
