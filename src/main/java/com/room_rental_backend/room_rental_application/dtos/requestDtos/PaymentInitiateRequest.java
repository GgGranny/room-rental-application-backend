package com.room_rental_backend.room_rental_application.dtos.requestDtos;

import com.room_rental_backend.room_rental_application.enums.PaymentGateway;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// New API: landlord starts a featuring payment for one of their properties.
public record PaymentInitiateRequest(
                @NotBlank(message = "propertyId is required") String propertyId,
                @NotNull(message = "gateway is required") PaymentGateway gateway) {
}
