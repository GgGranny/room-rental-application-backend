package com.room_rental_backend.room_rental_application.interfaces;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.room_rental_backend.room_rental_application.dtos.responseDtos.KycResponse;

public interface KycService {

    KycResponse submitKyc(String kycData, MultipartFile frontImage, MultipartFile backImage, MultipartFile selfie);

    List<Map<String, Object>> fetchAllKycs();

    KycResponse updateKyc(String request, MultipartFile frontImage, MultipartFile backImage, MultipartFile selfie);

    Map<String, String> deleteKyc(int kycId);
}
