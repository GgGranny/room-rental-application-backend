package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import java.math.BigDecimal;
import java.time.Instant;

import com.room_rental_backend.room_rental_application.enums.RoomStatus;
import com.room_rental_backend.room_rental_application.enums.RoomType;

// New API: a shared room the authenticated tenant has expressed roommate interest in.
public record RoomInterestResponse(
        String roomId,
        String roomTitle,
        String location,
        BigDecimal price,
        RoomStatus status,
        RoomType sharingType,
        Instant interestedSince) {
}
