package com.room_rental_backend.room_rental_application.services;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.room_rental_backend.room_rental_application.enums.ImageMetadataTypes;
import com.room_rental_backend.room_rental_application.exceptions.FileUploadException;
import com.room_rental_backend.room_rental_application.exceptions.UserAuthenticationException;
import com.room_rental_backend.room_rental_application.exceptions.UserNotFoundException;
import com.room_rental_backend.room_rental_application.interfaces.SupabaseFileStorageService;
import com.room_rental_backend.room_rental_application.models.ImageMetadata;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.repositories.ImageMetadataRepository;
import com.room_rental_backend.room_rental_application.repositories.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupabaseFileUploadServiceImple implements SupabaseFileStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String supabaseKey;

    @Value("${supabase.private-bucket-name}")
    private String privateBucketName;

    @Value("${supabase.public-bucket-name}")
    private String publicBucketName;

    private final RestTemplate restTemplate;

    private final ImageMetadataRepository imageMetadataRepository;

    private final UserRepository userRepository;

    @Transactional
    @Override
    public ImageMetadata uploadFile(MultipartFile file, String folderName, String bucketType, String metadataType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserAuthenticationException("User not authenticated");
        }

        String userEmail;
        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            userEmail = userDetails.getUsername();
        } else {
            userEmail = authentication.getName();
        }
        log.info("Authenticated user email: {}", userEmail);
        Users user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found in the database"));

        String fileExtension = getFileExtension(file);
        String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
        String storagePath = folderName + "/" + uniqueFileName;
        String bucketName = bucketType.equalsIgnoreCase("private") ? privateBucketName : publicBucketName;
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + storagePath;

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));
        headers.set("x-upsert", "true");

        try {
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class);
            System.out.println("Supabase upload response: " + response.getBody());
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to upload file to Supabase: {}", response.getBody());
                throw new FileUploadException("Supabase upload failed: " + response.getStatusCode());
            }

            ImageMetadataTypes metadataTypeEnum = Stream.of(ImageMetadataTypes.values())
                    .filter(type -> type.name().equalsIgnoreCase(metadataType))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid metadata type: " + metadataType));

            String fileUrl = bucketType.equalsIgnoreCase("private")
                    ? supabaseUrl + "/storage/v1/object/authenticated/" + bucketName + "/" + storagePath
                    : supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + storagePath;
            ImageMetadata metadata = ImageMetadata.builder()
                    .fileName(uniqueFileName)
                    .storagePath(storagePath)
                    .url(fileUrl)
                    .contentType(file.getContentType().toString())
                    .fileSize(Long.valueOf(file.getSize()))
                    .user(user)
                    .metadataType(metadataTypeEnum)
                    .build();
            return imageMetadataRepository.save(metadata);
        } catch (IOException e) {
            log.error("Error reading file bytes: {}", e.getMessage());
            throw new FileUploadException("Failed to read file bytes");
        } catch (HttpStatusCodeException e) {
            log.error("Supabase upload failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new FileUploadException("Supabase upload failed: " + e.getStatusCode());
        } catch (RestClientException e) {
            log.error("Supabase upload request failed: {}", e.getMessage());
            throw new FileUploadException("Supabase upload request failed");
        }
    }

    @Override
    public void deleteFile(Long metadataId, String bucket) {
        ImageMetadata metadata = imageMetadataRepository.findById(metadataId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + metadata.getStoragePath();

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, String.class);

        imageMetadataRepository.delete(metadata);
    }

    @Override
    public String getPublicUrl(String storagePath) {

        // TODO Auto-generated method stub
        return null;
    }

    // Validates the file type and size
    private void validateFile(MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("File is required");
        }
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        // Limit file size to 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds the maximum limit of 5MB");
        }

        // Only allow image files
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
    }

    // Extracts the file extension
    private String getFileExtension(MultipartFile file) {
        validateFile(file);
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("File must have an extension");
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
    }

}
