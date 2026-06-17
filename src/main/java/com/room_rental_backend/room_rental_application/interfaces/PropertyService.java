package com.room_rental_backend.room_rental_application.interfaces;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.PropertyRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.PropertyDetailsResponseDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.PropertyResponseDto;

public interface PropertyService {
    PropertyResponseDto createProperty(PropertyRequest propertyRequest, MultipartFile propertyThumbnail);

    List<PropertyResponseDto> fetchAllProperties();

    PropertyResponseDto updateProperty(String propertyId, PropertyRequest propertyRequest, MultipartFile propertyThumbnail);

    PropertyDetailsResponseDto fetchPropertyDetails(String propertyId);

    PropertyResponseDto updatedPropertyStatus(String propertyId, String status);

    boolean deleteProperty(String propertyId);
}
