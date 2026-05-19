package com.room_rental_backend.room_rental_application.mappers;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.CompleteUserProfileRequest;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.RegisterUserRequestDtos;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.UserResponse;
import com.room_rental_backend.room_rental_application.models.Users;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public Users toEntity(RegisterUserRequestDtos request) {
        Users user = new Users();
        user.setEmail(normalizeEmail(request.email()));
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setVerified(false);
        user.setActive(false);
        return user;
    }

    public UserResponse toDto(Users user) {
        return UserResponse.builder()
                .userId(user.getId())
                .role(user.getRoles())
                .fname(user.getFname())
                .lname(user.getLname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .profilePictureUrl(user.getProfilePictureUrl())
                .provider(user.getProvider())
                .verified(user.isVerified())
                .active(user.isActive())
                .profileCompleted(isProfileCompleted(user))
                .kycSubmitted(user.getKycUrl() != null)
                .kycStatus(user.getKycUrl() != null ? user.getKycUrl().getStatus() : null)
                .build();
    }

    public void updateProfile(Users user, CompleteUserProfileRequest request) {
        user.setFname(trimToNull(request.fname()));
        user.setLname(trimToNull(request.lname()));
        user.setDateOfBirth(trimToNull(request.dob()));
        user.setRoles(request.role());
    }

    public boolean isProfileCompleted(Users user) {
        return user != null
                && user.getRoles() != null
                && hasText(user.getFname())
                && hasText(user.getLname())
                && hasText(user.getDateOfBirth());
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String trimToNull(String value) {
        if (!hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
