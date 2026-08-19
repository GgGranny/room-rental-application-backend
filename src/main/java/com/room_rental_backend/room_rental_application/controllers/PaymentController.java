package com.room_rental_backend.room_rental_application.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.PaymentInitiateRequest;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.PaymentVerifyRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.PaymentInitiateResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.PaymentResponseDto;
import com.room_rental_backend.room_rental_application.interfaces.PaymentService;
import com.room_rental_backend.room_rental_application.responseHandler.ApiResponse;
import com.room_rental_backend.room_rental_application.responseHandler.GlobalResponseHandler;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// New API: featured-listing payment endpoints (landlords only).
// URL security: /api/v1/payments/** is LANDLORD-only in SpringSecurity; the
// service additionally verifies that the property being paid for belongs to the
// authenticated landlord.
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Landlord: create a payment attempt and get the gateway redirect info.
    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<PaymentInitiateResponse>> initiatePayment(
            @Valid @RequestBody PaymentInitiateRequest request,
            Authentication authentication) {
        PaymentInitiateResponse response = paymentService.initiatePayment(request, authentication);
        return GlobalResponseHandler.success("Payment initiated successfully", response, HttpStatus.CREATED);
    }

    // Landlord: confirm a payment after the gateway redirects back; on success
    // the property is marked featured.
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequest request,
            Authentication authentication) {
        PaymentResponseDto response = paymentService.verifyPayment(request, authentication);
        return GlobalResponseHandler.success("Payment verified successfully", response, HttpStatus.OK);
    }

    // Landlord: my featuring payment history.
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<PaymentResponseDto>>> getMyPayments(Authentication authentication) {
        List<PaymentResponseDto> response = paymentService.getMyPayments(authentication);
        return GlobalResponseHandler.success("Payment history fetched successfully", response, HttpStatus.OK);
    }
}
