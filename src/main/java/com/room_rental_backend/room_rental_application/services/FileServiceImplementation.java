package com.room_rental_backend.room_rental_application.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.room_rental_backend.room_rental_application.exceptions.FileUploadException;
import com.room_rental_backend.room_rental_application.interfaces.FileService;

@Component
public class FileServiceImplementation implements FileService {

    @Value("${file.kyc.folder}")
    private String uploadDir;

    @Override
    public String uploadFile(MultipartFile file) {
        validateFile(file);
        try {
            String fileName = generateFileName(file);
            Path uploadDirectory = Paths.get(uploadDir);
            Files.createDirectories(uploadDirectory);
            Path uploadPath = uploadDirectory.resolve(fileName);

            try (InputStream fs = file.getInputStream()) {
                Files.copy(fs, uploadPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return uploadPath.toString();
        } catch (IOException e) {
            throw new FileUploadException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public List<String> uploadMultipleFiles(List<MultipartFile> files) {
        return files.stream()
                .map(file -> uploadFile(file))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteFile(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        Path filePath = Path.of(StringUtils.cleanPath(path));
        try {
            Files.delete(filePath);
        } catch (NoSuchFileException e) {
            throw new RuntimeException("No Such File Found", e);
        } catch (IOException e) {
            throw new RuntimeException("File Failed to delete");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null) {
            throw new FileUploadException("File is required");
        }

        if (file.isEmpty()) {
            throw new FileUploadException("File is empty");
        }

        // check size — max 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new FileUploadException("File size must not exceed 5MB");
        }

        // check file type
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedType(contentType)) {
            throw new FileUploadException("Only JPG, PNG, PDF files are allowed");
        }
    }

    private boolean isAllowedType(String contentType) {
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("application/pdf");
    }

    private String generateFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new FileUploadException("File must have a name");
        }

        String originalName = StringUtils.cleanPath(originalFilename);
        String extension = StringUtils.getFilenameExtension(originalName);
        if (extension == null || extension.isBlank()) {
            throw new FileUploadException("File must have an extension");
        }
        return UUID.randomUUID() + "." + extension.toLowerCase();
    }

}
