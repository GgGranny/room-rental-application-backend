package com.room_rental_backend.room_rental_application.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.room_rental_backend.room_rental_application.dtos.responseDtos.AdminDashboardStatsDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.KycResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.PropertyResponseDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.UserResponse;
import com.room_rental_backend.room_rental_application.interfaces.AdminService;
import com.room_rental_backend.room_rental_application.responseHandler.ApiResponse;
import com.room_rental_backend.room_rental_application.responseHandler.GlobalResponseHandler;

import lombok.RequiredArgsConstructor;

// New API: admin moderation endpoints. The whole /api/v1/admin/** tree is
// restricted to ROLE_ADMIN in SpringSecurity, so no per-method guard is needed.
@RestController
@RequestMapping("/api/v1/admin/")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> response = adminService.getAllUsers();
        return GlobalResponseHandler.success("Users fetched successfully", response, HttpStatus.OK);
    }

    // Block (active=false) or reactivate (active=true) a tenant/landlord account.
    @PatchMapping("users/{userId}/status/{active}")
    public ResponseEntity<ApiResponse<UserResponse>> setUserActiveStatus(
            @PathVariable("userId") String userId,
            @PathVariable("active") boolean active) {
        UserResponse response = adminService.setUserActiveStatus(userId, active);
        return GlobalResponseHandler.success("User status updated successfully", response, HttpStatus.OK);
    }

    @GetMapping("kyc")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllKycs() {
        List<Map<String, Object>> response = adminService.getAllKycs();
        return GlobalResponseHandler.success("Kyc records fetched successfully", response, HttpStatus.OK);
    }

    // Admin-gated KYC decision (APPROVED / REJECTED / PENDING).
    @PatchMapping("kyc/{kycId}/{status}")
    public ResponseEntity<ApiResponse<KycResponse>> moderateKyc(
            @PathVariable("kycId") Integer kycId,
            @PathVariable("status") String status) {
        KycResponse response = adminService.moderateKyc(kycId, status);
        return GlobalResponseHandler.success("Kyc status updated successfully", response, HttpStatus.OK);
    }

    @GetMapping("properties")
    public ResponseEntity<ApiResponse<List<PropertyResponseDto>>> getAllProperties() {
        List<PropertyResponseDto> response = adminService.getAllProperties();
        return GlobalResponseHandler.success("Properties fetched successfully", response, HttpStatus.OK);
    }

    // Moderate a listing, e.g. BLOCKED_BY_ADMIN or reinstating to ACTIVE.
    @PatchMapping("properties/{propertyId}/status/{status}")
    public ResponseEntity<ApiResponse<PropertyResponseDto>> moderateProperty(
            @PathVariable("propertyId") String propertyId,
            @PathVariable("status") String status) {
        PropertyResponseDto response = adminService.moderateProperty(propertyId, status);
        return GlobalResponseHandler.success("Property status updated successfully", response, HttpStatus.OK);
    }

    @GetMapping("stats")
    public ResponseEntity<ApiResponse<AdminDashboardStatsDto>> getDashboardStats() {
        AdminDashboardStatsDto response = adminService.getDashboardStats();
        return GlobalResponseHandler.success("Dashboard stats fetched successfully", response, HttpStatus.OK);
    }
}
