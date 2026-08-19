package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record PropertyResponseDto(
        String id,
        String propertyName,
        String propertyStatus,
        String description,
        String thumbnailUrl,
        String country,

        // String city,
        // String district,
        // String province,
        // String zipCode,
        // String country,

        Integer totalRooms,

        // New API (featured listings): surfaced so the UI can badge/sort featured
        // properties.
        boolean featured,
        LocalDateTime featuredUntil) {
}
