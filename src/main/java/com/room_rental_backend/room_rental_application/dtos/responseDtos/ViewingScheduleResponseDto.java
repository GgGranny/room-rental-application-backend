package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import java.time.Instant;
import java.time.LocalDateTime;

import com.room_rental_backend.room_rental_application.enums.ScheduleStatus;

import lombok.Builder;

// New API: viewing request as seen by both tenant and landlord. Carries enough
// room/tenant/landlord summary so neither side needs a second lookup.
@Builder
public record ViewingScheduleResponseDto(
                String scheduleId,
                ScheduleStatus status,
                LocalDateTime scheduledAt,
                String note,
                String responseNote,
                Instant createdAt,

                String roomId,
                String roomTitle,
                String roomLocation,
                String propertyId,
                String propertyName,

                String tenantId,
                String tenantName,
                String tenantEmail,
                String tenantPhone,

                String landlordId,
                String landlordName,
                String landlordEmail,
                String landlordPhone) {
}
