package com.room_rental_backend.room_rental_application.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.room_rental_backend.room_rental_application.enums.ImageMetadataTypes;
import com.room_rental_backend.room_rental_application.models.ImageMetadata;
import com.room_rental_backend.room_rental_application.models.Users;

public interface ImageMetadataRepository extends JpaRepository<ImageMetadata, Long> {

    @Modifying
    @Query("DELETE FROM ImageMetadata data WHERE data.user.id = :id AND data.metadataType = :type")
    void deleteAllByUserId(String id, ImageMetadataTypes type);

    @Query("SELECT id FROM ImageMetadata data WHERE data.user.id = :userId AND data.metadataType = :type")
    List<Long> getAllKycMetadatasIds(String userId, ImageMetadataTypes type);

    List<ImageMetadata> findAllByUserIdAndMetadataTypeAndUrlIn(String userId, ImageMetadataTypes type, List<String> urls);

    ImageMetadata findByUser(Users user);

    List<ImageMetadata> findAllByRoomIdAndMetadataTypeOrderByUploadedAtAsc(String roomId, ImageMetadataTypes type);

    List<ImageMetadata> findAllByIdInAndRoomIdAndMetadataType(List<Long> roomIdsToRemove, String roomId,
            ImageMetadataTypes room);
}
