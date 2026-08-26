package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import java.math.BigDecimal;
import java.util.List;

import com.room_rental_backend.room_rental_application.enums.RoomStatus;
import com.room_rental_backend.room_rental_application.enums.RoomType;

import lombok.Builder;

// New API: single marker/card payload for the "Find Rooms Near You" map.
// Contains only public room data plus the distance from the searcher. The
// searcher's own coordinates are NEVER echoed back or stored — they are used
// server-side purely to compute distanceKm. All rooms returned are AVAILABLE.
@Builder
public record NearbyRoomResponse(
        String roomId,
        String roomTitle,
        String description,
        String location,
        BigDecimal price,
        RoomStatus status,
        String roomType,
        RoomType sharingType,
        Integer floorNumber,
        Integer totalRooms,
        String propertyId,
        String propertyName,
        Double latitude,
        Double longitude,
        // Great-circle distance (km) from the searcher to this room.
        Double distanceKm,
        List<ImageDataResponse> imageUrls) {
}
