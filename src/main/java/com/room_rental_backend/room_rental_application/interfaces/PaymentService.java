package com.room_rental_backend.room_rental_application.interfaces;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.PaymentInitiateRequest;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.PaymentVerifyRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.PaymentInitiateResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.PaymentResponseDto;

// New API: featured-listing payments for landlords. A landlord pays a fixed
// amount to feature one of their properties for a fixed number of days. Only the
// owning landlord may pay for a property; property upload itself stays free.
public interface PaymentService {

    // Create a payment attempt and return what the frontend needs to hand the
    // browser off to the selected gateway (eSewa POST form or Khalti GET url).
    PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request, Authentication authentication);

    // Confirm a payment with the gateway after redirect-back; on success mark the
    // payment SUCCESS and feature the property for the configured window.
    PaymentResponseDto verifyPayment(PaymentVerifyRequest request, Authentication authentication);

    // A landlord's own payment history, most recent first.
    List<PaymentResponseDto> getMyPayments(Authentication authentication);
}
