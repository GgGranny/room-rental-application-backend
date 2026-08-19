package com.room_rental_backend.room_rental_application.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

import com.room_rental_backend.room_rental_application.dtos.responseDtos.KycResponse;
import com.room_rental_backend.room_rental_application.interfaces.KycService;
import com.room_rental_backend.room_rental_application.responseHandler.ApiResponse;
import com.room_rental_backend.room_rental_application.responseHandler.GlobalResponseHandler;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycSerciveImplementation;

    // New API: returns only the authenticated tenant/landlord's KYC record for
    // client-side status guidance. KYC documents remain protected by existing access rules.
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<KycResponse>> getMyKyc(Authentication authentication) {
        KycResponse response = kycSerciveImplementation.getMyKyc(authentication);
        return GlobalResponseHandler.success("KYC status fetched successfully", response, HttpStatus.OK);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<KycResponse>> createKyc(
            @RequestPart("kycData") String kycDataJson,
            @RequestPart("frontImage") MultipartFile frontImage,
            @RequestPart(value = "backImage", required = false) MultipartFile backImage,
            @RequestPart("selfieImage") MultipartFile selfie) {
        KycResponse response = kycSerciveImplementation.submitKyc(kycDataJson, frontImage, backImage, selfie);
        return GlobalResponseHandler.success(
                "KYC submitted successfully",
                response,
                HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> kycs() {
        List<Map<String, Object>> response = kycSerciveImplementation.fetchAllKycs();
        return GlobalResponseHandler.success("KYC Fetched Successfully", response, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<ApiResponse<KycResponse>> putMethodName(
            @RequestPart("kycData") String kycDataJson,
            @RequestPart("frontImage") MultipartFile frontImage,
            @RequestPart(value = "backImage", required = false) MultipartFile backImage,
            @RequestPart("selfieImage") MultipartFile selfie) {
        KycResponse response = kycSerciveImplementation.updateKyc(kycDataJson, frontImage, backImage, selfie);
        return GlobalResponseHandler.success(
                "kyc updated success fully for user " + response.getCustomerId(),
                response,
                HttpStatus.OK);
    }

    @DeleteMapping("/{kycId}")
    public ResponseEntity<ApiResponse<Void>> removeKyc(@PathVariable("kycId") Integer kycId) {
        kycSerciveImplementation.deleteKyc(kycId);
        return GlobalResponseHandler.success(
                "Kyc deleted successfully",
                null,
                HttpStatus.OK);
    }

    @PatchMapping("/{kycId}/{status}")
    public ResponseEntity<ApiResponse<KycResponse>> updateKycStatus(
            @PathVariable("kycId") Integer kycId,
            @PathVariable("status") String status) {
        KycResponse response = kycSerciveImplementation.updateKycStatus(kycId, status);
        return GlobalResponseHandler.success(
                "Kyc status updated successfully",
                response,
                HttpStatus.OK);
    }
}
