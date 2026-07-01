package com.room_rental_backend.room_rental_application.mappers;

import java.util.List;
import java.util.Optional;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.PropertyRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.PropertyDetailsResponseDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.PropertyResponseDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoomResponseDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.UserResponse;
import com.room_rental_backend.room_rental_application.enums.ImageMetadataTypes;
import com.room_rental_backend.room_rental_application.enums.PropertyStatus;
import com.room_rental_backend.room_rental_application.models.Landlord;
import com.room_rental_backend.room_rental_application.models.Property;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.repositories.ImageMetadataRepository;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PropertyMapper {

        private final ImageMetadataMapper imageMetadataMapper;

        private final ImageMetadataRepository imageMetadataRepository;

        public Property toEntity(PropertyRequest request, String thumbnailUrl, Landlord landlord) {
                return Property.builder()
                                .country(request.country())
                                .city(request.city())
                                .description(request.description())
                                .district(request.district())
                                .landlord(landlord)
                                .propertyName(request.propertyName())
                                .propertyStatus(PropertyStatus.ACTIVE)
                                .province(request.province())
                                .thumbnailUrl(thumbnailUrl)
                                .zipCode(request.zipCode())
                                .build();
        }

        public PropertyResponseDto toPropertyResponseDto(Property property) {
                return PropertyResponseDto.builder()
                                .city(property.getCity())
                                .country(property.getCountry())
                                .description(property.getDescription())
                                .district(property.getDistrict())
                                .id(property.getId())
                                .propertyName(property.getPropertyName())
                                .propertyStatus(String.valueOf(property.getPropertyStatus().name()))
                                .province(property.getProvince())
                                .zipCode(property.getZipCode())
                                .thumbnailUrl(property.getThumbnailUrl())
                                .totalRooms(property.getRooms() == null ? 0 : property.getRooms().size())
                                .build();
        }

        public PropertyDetailsResponseDto toPropertyDetailsResponseDto(Property property) {
                Users user = property.getLandlord().getUser();
                UserResponse userResponse = UserResponse.builder()
                                .userId(user.getId())
                                .active(user.isActive())
                                .dateOfBirth(user.getDateOfBirth())
                                .email(user.getEmail())
                                .fname(user.getFname())
                                .lname(user.getLname())
                                .kycStatus(user.getKycUrl() != null ? user.getKycUrl().getStatus() : null)
                                .kycSubmitted(user.getKycUrl() != null)
                                .phoneNumber(user.getPhoneNumber())
                                .role(user.getRoles())
                                .profilePictureUrl(user.getProfilePictureUrl())
                                .verified(user.isVerified())
                                .build();

                List<RoomResponseDto> roomResponseDto = Optional.ofNullable(property.getRooms())
                                .orElse(List.of())
                                .stream()
                                .map(room -> RoomResponseDto.builder()
                                                .roomId(room.getId().toString())
                                                .roomTitle(room.getRoomTitle())
                                                .description(room.getDescription())
                                                .location(room.getLocation())
                                                .price(room.getPrice())
                                                .status(room.getStatus())
                                                .roomType(room.getRoomType())
                                                .floorNumber(room.getFloorNumber())
                                                .totalRooms(room.getTotalRooms())
                                                .imageUrls(imageMetadataRepository
                                                                .findAllByRoomIdAndMetadataTypeOrderByUploadedAtAsc(
                                                                                room.getId(), ImageMetadataTypes.ROOM)
                                                                .stream()
                                                                .map(imageMetadataMapper::toImageDataResponse)
                                                                .toList())
                                                .propertyId(property.getId().toString())
                                                .propertyName(property.getPropertyName())
                                                .city(property.getCity())
                                                .district(property.getDistrict())
                                                .build())
                                .toList();
                return PropertyDetailsResponseDto.builder()
                                .city(property.getCity())
                                .country(property.getCountry())
                                .description(property.getDescription())
                                .district(property.getDistrict())
                                .id(property.getId())
                                .propertyName(property.getPropertyName())
                                .propertyStatus(String.valueOf(property.getPropertyStatus().name()))
                                .province(property.getProvince())
                                .zipCode(property.getZipCode())
                                .thumbnailUrl(property.getThumbnailUrl())
                                .landlord(userResponse)
                                .rooms(roomResponseDto)
                                .build();
        }
}
