package com.room_rental_backend.room_rental_application.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.RoomRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.ImageDataResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoomDetailsResponseDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoomResponseDto;
import com.room_rental_backend.room_rental_application.enums.RoomStatus;
import com.room_rental_backend.room_rental_application.models.Property;
import com.room_rental_backend.room_rental_application.models.Room;

import io.jsonwebtoken.lang.Arrays;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoomMapper {

    private final UserMapper userMapper;

    public Room ToEntity(RoomRequest request) {
        Room room = Room.builder()
                .roomTitle(request.getRoomTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .price(request.getPrice())
                .status(request.getStatus() == null ? RoomStatus.AVAILABLE : request.getStatus())
                .preferredTenants(Optional.ofNullable(request.getPreferredTenants()).orElse(new ArrayList<>()))
                .facilities(cleanTextList(request.getFacilities()))
                .rules(cleanTextList(request.getRules()))
                .roomType(request.getRoomType())
                .sharingType(request.getSharingType())
                .floorNumber(request.getFloorNumber())
                .totalRooms(request.getTotalRooms())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
        return room;
    }

    public void updateEntity(Room room, RoomRequest request) {
        room.setRoomTitle(request.getRoomTitle());
        room.setDescription(request.getDescription());
        room.setLocation(request.getLocation());
        room.setPrice(request.getPrice());
        room.setStatus(request.getStatus() == null ? room.getStatus() : request.getStatus());
        room.setPreferredTenants(Optional.ofNullable(request.getPreferredTenants()).orElse(new ArrayList<>()));
        room.setRoomType(request.getRoomType());
        room.setSharingType(request.getSharingType());
        room.setFloorNumber(request.getFloorNumber());
        room.setTotalRooms(request.getTotalRooms());
        room.setAddress(request.getAddress());
        room.setLatitude(request.getLatitude());
        room.setLongitude(request.getLongitude());
        room.setFacilities(cleanTextList(request.getFacilities()));
        room.setRules(cleanTextList(request.getRules()));
    }

    public RoomResponseDto toRoomResponseDto(Room room, List<ImageDataResponse> roomImageUrls) {
        Property property = room.getProperty();
        return RoomResponseDto.builder()
                .roomId(room.getId())
                .roomTitle(room.getRoomTitle())
                .description(room.getDescription())
                .location(room.getLocation())
                .price(room.getPrice())
                .status(room.getStatus())
                .preferredTenants(Optional.ofNullable(room.getPreferredTenants()).orElse(new ArrayList<>()))
                .roomType(room.getRoomType())
                .sharingType(room.getSharingType())
                .floorNumber(room.getFloorNumber())
                .totalRooms(room.getTotalRooms())
                .imageUrls(Optional.ofNullable(roomImageUrls).orElse(new ArrayList<>()))
                .facilities(Optional.ofNullable(room.getFacilities()).orElse(new ArrayList<>()))
                .rules(Optional.ofNullable(room.getRules()).orElse(new ArrayList<>()))
                .propertyId(property == null ? null : property.getId())
                .propertyName(property == null ? null : property.getPropertyName())
                // .city(property == null ? null : property.getCity())
                // .district(property == null ? null : property.getDistrict())
                // .province(property == null ? null : property.getProvince())
                .build();
    }

    public RoomDetailsResponseDto toRoomDetailsResponseDto(Room room, List<ImageDataResponse> roomImageUrls) {
        Property property = room.getProperty();
        return RoomDetailsResponseDto.builder()
                .roomId(room.getId())
                .roomTitle(room.getRoomTitle())
                .description(room.getDescription())
                .location(room.getLocation())
                .price(room.getPrice())
                .status(room.getStatus())
                .preferredTenants(Optional.ofNullable(room.getPreferredTenants()).orElse(new ArrayList<>()))
                .roomType(room.getRoomType())
                .sharingType(room.getSharingType())
                .floorNumber(room.getFloorNumber())
                .totalRooms(room.getTotalRooms())
                .imageUrls(Optional.ofNullable(roomImageUrls).orElse(new ArrayList<>()))
                .facilities(Optional.ofNullable(room.getFacilities()).orElse(new ArrayList<>()))
                .rules(Optional.ofNullable(room.getRules()).orElse(new ArrayList<>()))
                .propertyId(property == null ? null : property.getId())
                .propertyName(property == null ? null : property.getPropertyName())
                // .city(property == null ? null : property.getCity())
                // .district(property == null ? null : property.getDistrict())
                // .province(property == null ? null : property.getProvince())
                .address(room.getAddress())
                .latitude(room.getLatitude())
                .Longitude(room.getLongitude())
                .userResponse(property == null || property.getLandlord() == null
                        ? null
                        : userMapper.toDto(property.getLandlord().getUser()))
                .build();
    }

    private List<String> cleanTextList(List<String> values) {
        return Optional.ofNullable(values)
                .orElse(new ArrayList<>())
                .stream()
                .filter(this::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
