package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import java.math.BigDecimal;
import java.util.Map;

import com.room_rental_backend.room_rental_application.enums.PaymentGateway;

import lombok.Builder;

// New API: everything the frontend needs to hand the user off to the gateway.
// For eSewa, redirectMethod is "POST" and formFields carries the signed hidden
// inputs to auto-submit to redirectUrl. For Khalti, redirectMethod is "GET" and
// the browser is simply sent to redirectUrl (formFields is null).
@Builder
public record PaymentInitiateResponse(
                String paymentId,
                String transactionUuid,
                PaymentGateway gateway,
                BigDecimal amount,
                String redirectMethod,
                String redirectUrl,
                Map<String, String> formFields) {
}
