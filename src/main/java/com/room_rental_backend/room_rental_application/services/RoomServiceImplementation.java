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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.RoomRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.ImageDataResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.NearbyRoomResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoomDetailsResponseDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoomResponseDto;
import com.room_rental_backend.room_rental_application.enums.ImageMetadataTypes;
import com.room_rental_backend.room_rental_application.enums.RoomStatus;
import com.room_rental_backend.room_rental_application.enums.KycStatus;
import com.room_rental_backend.room_rental_application.enums.Roles;
import com.room_rental_backend.room_rental_application.exceptions.UnauthorizedException;
import com.room_rental_backend.room_rental_application.interfaces.RoomService;
import com.room_rental_backend.room_rental_application.interfaces.SupabaseFileStorageService;
import com.room_rental_backend.room_rental_application.mappers.ImageMetadataMapper;
import com.room_rental_backend.room_rental_application.mappers.RoomMapper;
import com.room_rental_backend.room_rental_application.models.ImageMetadata;
import com.room_rental_backend.room_rental_application.models.Property;
import com.room_rental_backend.room_rental_application.models.Room;
import com.room_rental_backend.room_rental_application.models.Landlord;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.models.filters.RoomSearchFilter;
import com.room_rental_backend.room_rental_application.repositories.ImageMetadataRepository;
import com.room_rental_backend.room_rental_application.repositories.PropertyRepository;
import com.room_rental_backend.room_rental_application.repositories.RoomRepository;
import com.room_rental_backend.room_rental_application.repositories.LandlordRepository;
import com.room_rental_backend.room_rental_application.repositories.UserRepository;
import com.room_rental_backend.room_rental_application.repositories.KycRepository;
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

    private final UserRepository userRepository;

    private final LandlordRepository landlordRepository;

    private final KycRepository kycRepository;

    private final com.room_rental_backend.room_rental_application.components.RoommateEventPublisher roommateEventPublisher;

    @Value("${supabase.public-bucket-name}")
    private String publicBucketName;

    @Transactional
    @Override
    public RoomDetailsResponseDto createRoom(RoomRequest request, List<MultipartFile> roomImages) {
        Property property = findPropertyById(request.getPropertyId());
        if (property == null) {
            throw new PropertyNotFoundException("property for id: " + request.getPropertyId() + " not found");
        }
        requireVerifiedOwner(property, true);
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
        requireVerifiedOwner(room.getProperty(), false);
        if (request.getPropertyId() != null && !request.getPropertyId().equals(room.getProperty().getId())) {
            Property requestedProperty = findPropertyById(request.getPropertyId());
            requireVerifiedOwner(requestedProperty, false);
        }
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
        requireVerifiedOwner(room.getProperty(), false);
        RoomStatus roomStatus = Stream.of(RoomStatus.values())
                .filter(value -> value.name().equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid room status: " + status));

        room.setStatus(roomStatus);
        Room savedRoom = roomRepository.save(room);
        // New API: real-time UI sync for open roommate maps when a SHARED room's
        // availability changes. Security is still enforced by roommate endpoints.
        if (savedRoom.getSharingType() == com.room_rental_backend.room_rental_application.enums.RoomType.SHARED) {
            roommateEventPublisher.publish(savedRoom.getId(),
                    com.room_rental_backend.room_rental_application.components.RoommateEventPublisher.ROOM_STATUS_CHANGED,
                    null, savedRoom.getStatus().name());
        }
        return roomMapper.toRoomDetailsResponseDto(savedRoom, getRoomImageResponses(savedRoom.getId()));
    }

    @Transactional
    @Override
    public void deleteRoom(String roomId) {
        Room room = findRoomById(roomId);
        requireVerifiedOwner(room.getProperty(), false);
        roomRepository.delete(room);
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

    // New API: "Find Rooms Near You". Validates the incoming coordinates/radius,
    // then delegates distance filtering to the database (Haversine query). Only
    // AVAILABLE, non-deleted rooms with coordinates are returned, closest first.
    // The searcher's coordinates are used only for the query and are never stored
    // or returned in the response.
    private static final double MAX_RADIUS_KM = 50.0;

    @Transactional(readOnly = true)
    @Override
    public List<NearbyRoomResponse> getNearbyRooms(double latitude, double longitude, double radiusKm) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitude must be between -180 and 180");
        }
        if (radiusKm <= 0) {
            throw new IllegalArgumentException("radius must be greater than 0");
        }
        double effectiveRadius = Math.min(radiusKm, MAX_RADIUS_KM);

        List<Object[]> rows = roomRepository.findNearbyAvailableRooms(latitude, longitude, effectiveRadius);
        if (rows.isEmpty()) {
            return List.of();
        }

        // Preserve the DB ordering (closest first) while loading full room data.
        List<String> orderedIds = new ArrayList<>();
        java.util.Map<String, Double> distanceById = new java.util.HashMap<>();
        for (Object[] row : rows) {
            String id = (String) row[0];
            double distance = ((Number) row[1]).doubleValue();
            orderedIds.add(id);
            distanceById.put(id, distance);
        }

        java.util.Map<String, Room> roomsById = roomRepository.findByIdIn(orderedIds).stream()
                .collect(Collectors.toMap(Room::getId, room -> room));

        return orderedIds.stream()
                .map(roomsById::get)
                .filter(java.util.Objects::nonNull)
                .map(room -> toNearbyRoomResponse(room, distanceById.get(room.getId())))
                .toList();
    }

    private NearbyRoomResponse toNearbyRoomResponse(Room room, Double distanceKm) {
        Property property = room.getProperty();
        return NearbyRoomResponse.builder()
                .roomId(room.getId())
                .roomTitle(room.getRoomTitle())
                .description(room.getDescription())
                .location(room.getLocation())
                .price(room.getPrice())
                .status(room.getStatus())
                .roomType(room.getRoomType())
                .sharingType(room.getSharingType())
                .floorNumber(room.getFloorNumber())
                .totalRooms(room.getTotalRooms())
                .propertyId(property != null ? property.getId() : null)
                .propertyName(property != null ? property.getPropertyName() : null)
                .latitude(room.getLatitude())
                .longitude(room.getLongitude())
                .distanceKm(distanceKm == null ? null : Math.round(distanceKm * 100.0) / 100.0)
                .imageUrls(getRoomImageResponses(room.getId()))
                .build();
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

    private void requireVerifiedOwner(Property property, boolean requireVerifiedKyc) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Authentication is required");
        }
        Users user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
        if (user.getRoles() != Roles.ROLE_LANDLORD) {
            throw new UnauthorizedException("Only landlords can manage rooms");
        }
        Landlord landlord = landlordRepository.findByUser(user);
        if (landlord == null || !landlord.getId().equals(property.getLandlord().getId())) {
            throw new UnauthorizedException("You can only manage rooms in your own properties");
        }
        if (!requireVerifiedKyc) {
            return;
        }
        KycStatus status = kycRepository.findByUserId(user.getId()).map(kyc -> kyc.getStatus()).orElse(null);
        if (status == null) {
            throw new UnauthorizedException("KYC verification is required before you can post a room. Please complete and verify your KYC first.");
        }
        if (status == KycStatus.PENDING) {
            throw new UnauthorizedException("Your KYC is currently under review. You can post a room after your KYC has been verified.");
        }
        if (status != KycStatus.APPROVED) {
            throw new UnauthorizedException("Your KYC has not been verified. Please update and resubmit your KYC before posting a room.");
        }
    }

}
