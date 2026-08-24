package com.room_rental_backend.room_rental_application.interfaces;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

import com.room_rental_backend.room_rental_application.dtos.responseDtos.KycResponse;

public interface KycService {

    // The KYC owner is always resolved from the authenticated principal;
    // any customerId supplied by the client is ignored.
    KycResponse submitKyc(String kycData, MultipartFile frontImage, MultipartFile backImage, MultipartFile selfie,
            Authentication authentication);

    List<Map<String, Object>> fetchAllKycs();

    KycResponse updateKyc(String request, MultipartFile frontImage, MultipartFile backImage, MultipartFile selfie);

    void deleteKyc(Integer kycId);

    KycResponse updateKycStatus(Integer kycId, String status);

    KycResponse getMyKyc(Authentication authentication);
}
