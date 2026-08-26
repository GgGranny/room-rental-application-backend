package com.room_rental_backend.room_rental_application.interfaces;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.RoomRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoomDetailsResponseDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoomResponseDto;
import com.room_rental_backend.room_rental_application.models.filters.RoomSearchFilter;

public interface RoomService {
    // create room
    RoomDetailsResponseDto createRoom(RoomRequest request, List<MultipartFile> roomImages);

    // update room
    RoomDetailsResponseDto updateRoom(String roomId, RoomRequest request, List<MultipartFile> roomImages,
            List<Long> roomIdsToRemove);

    // updated Room Status
    RoomDetailsResponseDto updatedRoomStatus(String roomId, String status);

    // Delete Room
    void deleteRoom(String roomId);

    // Get A Room Detail
    RoomDetailsResponseDto getRoomDetailsForOne(String roomId);

    // Get All Rooms
    List<RoomResponseDto> getAllRecommendedRooms();

    // Get All Featured Rooms
    List<RoomResponseDto> getAllFeaturedRooms(List<String> roomIds);

    // Search Rooms
    List<RoomResponseDto> searchRooms(RoomSearchFilter filter);

    // New API: rooms near a coordinate, within radiusKm, AVAILABLE only, closest
    // first. Distance filtering happens in the database (see RoomRepository).
    List<com.room_rental_backend.room_rental_application.dtos.responseDtos.NearbyRoomResponse> getNearbyRooms(
            double latitude, double longitude, double radiusKm);
}
