package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import java.time.Instant;
import java.time.LocalDateTime;
import com.room_rental_backend.room_rental_application.enums.NotificationType;

public record UserNotificationResponse(String id, String title, String body, NotificationType type,
                String referenceId, boolean read, Instant createdAt) {
}
