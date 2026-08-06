package com.room_rental_backend.room_rental_application.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.RoomRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.ImageDataResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoomDetailsResponseDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoomResponseDto;
import com.room_rental_backend.room_rental_application.enums.ImageMetadataTypes;
import com.room_rental_backend.room_rental_application.enums.RoomStatus;
import com.room_rental_backend.room_rental_application.interfaces.RoomService;
import com.room_rental_backend.room_rental_application.interfaces.SupabaseFileStorageService;
import com.room_rental_backend.room_rental_application.mappers.ImageMetadataMapper;
import com.room_rental_backend.room_rental_application.mappers.RoomMapper;
import com.room_rental_backend.room_rental_application.models.ImageMetadata;
import com.room_rental_backend.room_rental_application.models.Property;
import com.room_rental_backend.room_rental_application.models.Room;
import com.room_rental_backend.room_rental_application.models.filters.RoomSearchFilter;
import com.room_rental_backend.room_rental_application.repositories.ImageMetadataRepository;
import com.room_rental_backend.room_rental_application.repositories.PropertyRepository;
import com.room_rental_backend.room_rental_application.repositories.RoomRepository;
import com.room_rental_backend.room_rental_application.specifications.RoomSpecifications;

import jakarta.el.PropertyNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomServiceImplementation implements RoomService {

    private final RoomRepository roomRepository;

    private final PropertyRepository propertyRepository;

    private final RoomMapper roomMapper;

    private final SupabaseFileStorageService supabaseFileStorageService;

    private final ImageMetadataMapper imageMetadataMapper;

    private final ImageMetadataRepository imageMetadataRepository;

    @Value("${supabase.public-bucket-name}")
    private String publicBucketName;

    @Transactional
    @Override
    public RoomDetailsResponseDto createRoom(RoomRequest request, List<MultipartFile> roomImages) {
        Property property = findPropertyById(request.getPropertyId());
        if (property == null) {
            throw new PropertyNotFoundException("property for id: " + request.getPropertyId() + " not found");
        }
        Room room = roomMapper.ToEntity(request);
        room.setProperty(property);
        Room savedRoom = roomRepository.save(room);
        List<ImageDataResponse> imageDataResponses = uploadRoomImages(savedRoom, roomImages);

        return roomMapper.toRoomDetailsResponseDto(savedRoom, imageDataResponses);
    }

    @Transactional
    @Override
    public RoomDetailsResponseDto updateRoom(String roomId, RoomRequest request, List<MultipartFile> roomImages,
            List<Long> roomIdsToRemove) {
        Room room = findRoomById(roomId);
        roomMapper.updateEntity(room, request);

        if (request.getPropertyId() != null) {
            room.setProperty(findPropertyById(request.getPropertyId()));
        }
        // Delete Room Images from Supabase and Database
        deleteRoomImages(roomIdsToRemove, roomId);
        Room savedRoom = roomRepository.save(room);
        List<ImageDataResponse> roomImageResponses = getRoomImageResponses(savedRoom.getId());
        roomImageResponses = Stream
                .concat(roomImageResponses.stream(), uploadRoomImages(savedRoom, roomImages).stream())
                .toList();

        return roomMapper.toRoomDetailsResponseDto(savedRoom, roomImageResponses);
    }

    // Deletes The Room Images from Supabase and Database based on the provided
    // roomIdsToRemove
    private void deleteRoomImages(List<Long> roomIdsToRemove, String roomId) {
        if (roomIdsToRemove == null || roomIdsToRemove.isEmpty()) {
            return;
        }
        List<ImageMetadata> imagesToRemove = imageMetadataRepository
                .findAllByIdInAndRoomIdAndMetadataType(roomIdsToRemove, roomId, ImageMetadataTypes.ROOM);
        if (!imagesToRemove.isEmpty()) {
            imagesToRemove.stream()
                    .forEach(data -> supabaseFileStorageService.deleteFile(data.getId(), publicBucketName));
            imageMetadataRepository.deleteAll(imagesToRemove);
        }
    }

    @Transactional
    @Override
    public RoomDetailsResponseDto updatedRoomStatus(String roomId, String status) {
        Room room = findRoomById(roomId);
        RoomStatus roomStatus = Stream.of(RoomStatus.values())
                .filter(value -> value.name().equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid room status: " + status));

        room.setStatus(roomStatus);
        Room savedRoom = roomRepository.save(room);
        return roomMapper.toRoomDetailsResponseDto(savedRoom, getRoomImageResponses(savedRoom.getId()));
    }

    @Transactional
    @Override
    public void deleteRoom(String roomId) {
        roomRepository.delete(findRoomById(roomId));
    }

    @Transactional(readOnly = true)
    @Override
    public RoomDetailsResponseDto getRoomDetailsForOne(String roomId) {
        Room room = findRoomById(roomId);
        return roomMapper.toRoomDetailsResponseDto(room, getRoomImageResponses(room.getId()));
    }

    @Transactional(readOnly = true)
    @Override
    public List<RoomResponseDto> getAllRecommendedRooms() {
        return roomRepository.findTop12ByStatusOrderByCreatedAtDesc(RoomStatus.AVAILABLE)
                .stream()
                .map(room -> roomMapper.toRoomResponseDto(room, getRoomImageResponses(room.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<RoomResponseDto> getAllFeaturedRooms(List<String> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            return List.of();
        }
        return roomRepository.findByIdIn(roomIds)
                .stream()
                .map(room -> roomMapper.toRoomResponseDto(room, getRoomImageResponses(room.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<RoomResponseDto> searchRooms(RoomSearchFilter filter) {
        Specification<Room> specification = RoomSpecifications.filterRooms(
                filter == null ? RoomSearchFilter.builder().build() : filter);
        return roomRepository.findAll(specification)
                .stream()
                .map(room -> roomMapper.toRoomResponseDto(room, getRoomImageResponses(room.getId())))
                .toList();
    }

    private Room findRoomById(String roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found for id: " + roomId));
    }

    private Property findPropertyById(String propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found for id: " + propertyId));
    }

    private List<ImageDataResponse> uploadRoomImages(Room room, List<MultipartFile> roomImages) {
        if (roomImages == null || roomImages.isEmpty()) {
            return List.of();
        }
        return roomImages
                .stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(file -> supabaseFileStorageService.uploadFile(file, "rooms", publicBucketName, "ROOM"))
                .peek(metadata -> metadata.setRoom(room))
                .map(imageMetadataRepository::save)
                .map(imageMetadataMapper::toImageDataResponse)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<ImageDataResponse> getRoomImageResponses(String roomId) {
        return imageMetadataRepository
                .findAllByRoomIdAndMetadataTypeOrderByUploadedAtAsc(roomId, ImageMetadataTypes.ROOM)
                .stream()
                .map(imageMetadataMapper::toImageDataResponse)
                .toList();
    }

}
