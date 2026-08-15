package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import lombok.Builder;

// New API DTO: aggregate counts for the admin dashboard overview cards.
@Builder
public record AdminDashboardStatsDto(
                long totalUsers,
                long totalTenants,
                long totalLandlords,
                long totalAdmins,

                long totalProperties,
                long activeProperties,
                long blockedProperties,

                long totalKyc,
                long pendingKyc,
                long approvedKyc,
                long rejectedKyc) {
}
