package com.room_rental_backend.room_rental_application.services;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.room_rental_backend.room_rental_application.dtos.responseDtos.AdminDashboardStatsDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.KycResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.PropertyResponseDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.UserResponse;
import com.room_rental_backend.room_rental_application.enums.KycStatus;
import com.room_rental_backend.room_rental_application.enums.PropertyStatus;
import com.room_rental_backend.room_rental_application.enums.Roles;
import com.room_rental_backend.room_rental_application.exceptions.UnauthorizedException;
import com.room_rental_backend.room_rental_application.exceptions.UserNotFoundException;
import com.room_rental_backend.room_rental_application.interfaces.AdminService;
import com.room_rental_backend.room_rental_application.interfaces.KycService;
import com.room_rental_backend.room_rental_application.interfaces.PropertyService;
import com.room_rental_backend.room_rental_application.interfaces.UserService;
import com.room_rental_backend.room_rental_application.mappers.UserMapper;
import com.room_rental_backend.room_rental_application.models.Kyc;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.repositories.KycRepository;
import com.room_rental_backend.room_rental_application.repositories.PropertyRepository;
import com.room_rental_backend.room_rental_application.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// New API: admin moderation service. Delegates to the existing kyc/property
// services for status changes so their validation/mapping logic stays the single
// source of truth; only user activation and dashboard aggregation are new here.
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImplementation implements AdminService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserService userService;
    private final KycService kycService;
    private final KycRepository kycRepository;
    private final PropertyService propertyService;
    private final PropertyRepository propertyRepository;

    @Override
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Transactional
    @Override
    public UserResponse setUserActiveStatus(String userId, boolean active) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found for id: " + userId));

        // Never let an admin block/deactivate another admin account.
        if (user.getRoles() == Roles.ROLE_ADMIN) {
            throw new UnauthorizedException("Admin accounts cannot be blocked");
        }

        user.setActive(active);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public List<Map<String, Object>> getAllKycs() {
        return kycService.fetchAllKycs();
    }

    @Override
    public KycResponse moderateKyc(Integer kycId, String status) {
        // Reuses the existing status-transition logic; access is admin-gated in security config.
        return kycService.updateKycStatus(kycId, status);
    }

    @Override
    public List<PropertyResponseDto> getAllProperties() {
        return propertyService.fetchAllProperties();
    }

    @Override
    public PropertyResponseDto moderateProperty(String propertyId, String status) {
        return propertyService.updatedPropertyStatus(propertyId, status);
    }

    @Override
    public AdminDashboardStatsDto getDashboardStats() {
        List<Users> users = userRepository.findAll();
        long totalTenants = users.stream().filter(u -> u.getRoles() == Roles.ROLE_USER).count();
        long totalLandlords = users.stream().filter(u -> u.getRoles() == Roles.ROLE_LANDLORD).count();
        long totalAdmins = users.stream().filter(u -> u.getRoles() == Roles.ROLE_ADMIN).count();

        long totalProperties = propertyRepository.count();
        long activeProperties = propertyRepository.findAll().stream()
                .filter(p -> p.getPropertyStatus() == PropertyStatus.ACTIVE).count();
        long blockedProperties = propertyRepository.findAll().stream()
                .filter(p -> p.getPropertyStatus() == PropertyStatus.BLOCKED_BY_ADMIN).count();

        List<Kyc> kycs = kycRepository.findAll();
        long pendingKyc = kycs.stream().filter(k -> k.getStatus() == KycStatus.PENDING).count();
        long approvedKyc = kycs.stream().filter(k -> k.getStatus() == KycStatus.APPROVED).count();
        long rejectedKyc = kycs.stream().filter(k -> k.getStatus() == KycStatus.REJECTED).count();

        return AdminDashboardStatsDto.builder()
                .totalUsers(users.size())
                .totalTenants(totalTenants)
                .totalLandlords(totalLandlords)
                .totalAdmins(totalAdmins)
                .totalProperties(totalProperties)
                .activeProperties(activeProperties)
                .blockedProperties(blockedProperties)
                .totalKyc(kycs.size())
                .pendingKyc(pendingKyc)
                .approvedKyc(approvedKyc)
                .rejectedKyc(rejectedKyc)
                .build();
    }
}
