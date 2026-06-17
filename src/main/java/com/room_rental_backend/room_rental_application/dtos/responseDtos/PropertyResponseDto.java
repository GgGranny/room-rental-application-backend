package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import lombok.Builder;

@Builder
public record PropertyResponseDto(
        String id,
        String propertyName,
        String propertyStatus,
        String description,
        String thumbnailUrl,

        String address,
        String city,
        String district,
        String province,
        String zipCode,
        String country,

        Integer totalRooms) {
}
