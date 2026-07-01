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

        String thumbnailUrl = uploadThumbnail(propertyThumbnail);
        Property property = propertyMapper.toEntity(propertyRequest, thumbnailUrl, landlord);
        Property savedProperty = propertyRepository.save(property);
        return propertyMapper.toPropertyResponseDto(savedProperty);
    }

    @Override
    public List<PropertyResponseDto> fetchAllProperties() {
        return propertyRepository.findAll()
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

        property.setPropertyName(propertyRequest.propertyName());
        property.setLandlord(landlord);
        property.setPropertyStatus(propertyRequest.propertyStatus());
        property.setDescription(propertyRequest.description());
        property.setCity(propertyRequest.city());
        property.setDistrict(propertyRequest.district());
        property.setProvince(propertyRequest.province());
        property.setZipCode(propertyRequest.zipCode());
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
        return propertyMapper.toPropertyDetailsResponseDto(findPropertyById(propertyId));
    }

    @Transactional
    @Override
    public PropertyResponseDto updatedPropertyStatus(String propertyId, String status) {
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

}
