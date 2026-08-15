package com.room_rental_backend.room_rental_application.interfaces;

import java.util.List;
import java.util.Map;

import com.room_rental_backend.room_rental_application.dtos.responseDtos.AdminDashboardStatsDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.KycResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.PropertyResponseDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.UserResponse;

// New API: admin-only moderation surface. Implementations compose the existing
// user / kyc / property services so business logic is not duplicated.
public interface AdminService {

    List<UserResponse> getAllUsers();

    // Block (active=false) or reactivate (active=true) a non-admin account.
    UserResponse setUserActiveStatus(String userId, boolean active);

    List<Map<String, Object>> getAllKycs();

    // Admin-gated KYC decision (APPROVED / REJECTED / PENDING).
    KycResponse moderateKyc(Integer kycId, String status);

    List<PropertyResponseDto> getAllProperties();

    // Moderate a listing, e.g. BLOCKED_BY_ADMIN or reinstating to ACTIVE.
    PropertyResponseDto moderateProperty(String propertyId, String status);

    AdminDashboardStatsDto getDashboardStats();
}
