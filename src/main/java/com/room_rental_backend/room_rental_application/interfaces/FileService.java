package com.room_rental_backend.room_rental_application.interfaces;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String uploadFile(MultipartFile file);

    List<String> uploadMultipleFiles(List<MultipartFile> files);

    void deleteFile(String path);
}
