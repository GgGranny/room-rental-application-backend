package com.room_rental_backend.room_rental_application.interfaces;

import org.springframework.web.multipart.MultipartFile;

import com.room_rental_backend.room_rental_application.models.ImageMetadata;

public interface SupabaseFileStorageService {
    ImageMetadata uploadFile(MultipartFile file, String folderName, String bucketType, String metadataType);

    void deleteFile(Long metadataId, String bucket);

    String getPublicUrl(String storagePath);

}
