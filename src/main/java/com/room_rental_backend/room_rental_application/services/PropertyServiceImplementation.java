package com.room_rental_backend.room_rental_application.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.room_rental_backend.room_rental_application.models.ImageMetadata;
import com.room_rental_backend.room_rental_application.repositories.ImageMetadataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.PropertyRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.PropertyDetailsResponseDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.PropertyResponseDto;
import com.room_rental_backend.room_rental_application.enums.PropertyStatus;
import com.room_rental_backend.room_rental_application.interfaces.PropertyService;
import com.room_rental_backend.room_rental_application.interfaces.SupabaseFileStorageService;
import com.room_rental_backend.room_rental_application.mappers.PropertyMapper;
import com.room_rental_backend.room_rental_application.models.Landlord;
import com.room_rental_backend.room_rental_application.models.Property;
import com.room_rental_backend.room_rental_application.repositories.LandlordRepository;
import com.room_rental_backend.room_rental_application.repositories.PropertyRepository;
import com.room_rental_backend.room_rental_application.repositories.UserRepository;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.enums.Roles;
import com.room_rental_backend.room_rental_application.exceptions.UnauthorizedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PropertyServiceImplementation implements PropertyService {

    private final PropertyRepository propertyRepository;

    private final LandlordRepository landlordRepository;

    private final PropertyMapper propertyMapper;

    private final SupabaseFileStorageService supabaseFileStorageService;

    private final ImageMetadataRepository imageMetadataRepository;

    private final UserRepository userRepository;

    @Value("${supabase.public-bucket-name}")
    private String publicBucket;

    @Transactional
    @Override
    public PropertyResponseDto createProperty(PropertyRequest propertyRequest, MultipartFile propertyThumbnail) {
        Landlord landlord = landlordRepository.findById(String.valueOf(propertyRequest.landlordId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Landlord not found for id: " + propertyRequest.landlordId()));
        requireOwner(landlord);

        String thumbnailUrl = uploadThumbnail(propertyThumbnail);
        Property property = propertyMapper.toEntity(propertyRequest, thumbnailUrl, landlord);
        Property savedProperty = propertyRepository.save(property);
        return propertyMapper.toPropertyResponseDto(savedProperty);
    }

    @Override
    public List<PropertyResponseDto> fetchAllProperties() {
        List<Property> properties = isAdmin() ? propertyRepository.findAll()
                : propertyRepository.findByLandlordId(currentLandlord().getId());
        return properties
                .stream()
                .map(propertyMapper::toPropertyResponseDto)
                .toList();
    }

    @Transactional
    @Override
    public PropertyResponseDto updateProperty(
            String propertyId,
            PropertyRequest propertyRequest,
            MultipartFile propertyThumbnail) {
        Property property = findPropertyById(propertyId);
        Landlord landlord = landlordRepository.findById(String.valueOf(propertyRequest.landlordId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Landlord not found for id: " + propertyRequest.landlordId()));
        requireOwner(property.getLandlord());
        requireOwner(landlord);

        property.setPropertyName(propertyRequest.propertyName());
        property.setLandlord(landlord);
        property.setDescription(propertyRequest.description());
        // property.setCity(propertyRequest.city());
        // property.setDistrict(propertyRequest.district());
        // property.setProvince(propertyRequest.province());
        // property.setZipCode(propertyRequest.zipCode());
        property.setCountry(propertyRequest.country());

        if (propertyThumbnail != null && !propertyThumbnail.isEmpty()) {
            ImageMetadata imageMetadata = imageMetadataRepository.findByUser(landlord.getUser());
            if (imageMetadata != null) {
                supabaseFileStorageService.deleteFile(imageMetadata.getId(), publicBucket);
            }
            property.setThumbnailUrl(uploadThumbnail(propertyThumbnail));
        }

        return propertyMapper.toPropertyResponseDto(propertyRepository.save(property));
    }

    @Override
    public PropertyDetailsResponseDto fetchPropertyDetails(String propertyId) {
        Property property = findPropertyById(propertyId);
        requireOwner(property.getLandlord());
        return propertyMapper.toPropertyDetailsResponseDto(property);
    }

    @Transactional
    @Override
    public PropertyResponseDto updatedPropertyStatus(String propertyId, String status) {
        if (!isAdmin()) {
            throw new UnauthorizedException("Only an administrator can change property verification status");
        }
        Property property = findPropertyById(propertyId);
        PropertyStatus propertyStatus = Stream.of(PropertyStatus.values())
                .filter(value -> value.name().equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid property status: " + status));

        property.setPropertyStatus(propertyStatus);
        return propertyMapper.toPropertyResponseDto(propertyRepository.save(property));
    }

    @Transactional
    @Override
    public boolean deleteProperty(String propertyId) {
        Property property = findPropertyById(propertyId);
        requireOwner(property.getLandlord());
        ImageMetadata imageMetadata = imageMetadataRepository.findByUser(property.getLandlord().getUser());
        if (imageMetadata != null) {
            supabaseFileStorageService.deleteFile(imageMetadata.getId(), publicBucket);
        }
        propertyRepository.delete(property);
        return true;
    }

    private Property findPropertyById(String propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found for id: " + propertyId));
    }

    private String uploadThumbnail(MultipartFile propertyThumbnail) {
        if (propertyThumbnail == null || propertyThumbnail.isEmpty()) {
            return null;
        }
        return supabaseFileStorageService.uploadFile(propertyThumbnail, "property", "public", "PROPERTY").getUrl();
    }

    private Landlord currentLandlord() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Authentication is required");
        }
        Users user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
        if (user.getRoles() != Roles.ROLE_LANDLORD) {
            throw new UnauthorizedException("Only landlords can manage properties");
        }
        Landlord landlord = landlordRepository.findByUser(user);
        if (landlord == null) {
            throw new UnauthorizedException("Landlord profile not found");
        }
        return landlord;
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private void requireOwner(Landlord landlord) {
        if (!isAdmin() && !currentLandlord().getId().equals(landlord.getId())) {
            throw new UnauthorizedException("You can only manage your own properties");
        }
    }

}
