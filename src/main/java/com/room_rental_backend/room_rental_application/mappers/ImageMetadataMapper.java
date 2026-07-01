package com.room_rental_backend.room_rental_application.mappers;

import org.springframework.stereotype.Component;

import com.room_rental_backend.room_rental_application.dtos.responseDtos.ImageDataResponse;
import com.room_rental_backend.room_rental_application.models.ImageMetadata;

@Component
public class ImageMetadataMapper {

    public ImageDataResponse toImageDataResponse(ImageMetadata metadata) {
        return ImageDataResponse.builder()
                .contentType(metadata.getContentType())
                .fileSize((double) metadata.getFileSize())
                .id(metadata.getId())
                .imageFor(metadata.getMetadataType().toString())
                .url(metadata.getUrl())
                .build();
    }
}
