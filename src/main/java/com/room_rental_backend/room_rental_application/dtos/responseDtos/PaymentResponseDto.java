package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import com.room_rental_backend.room_rental_application.enums.PaymentGateway;
import com.room_rental_backend.room_rental_application.enums.PaymentStatus;

import lombok.Builder;

// New API: summary of a featuring payment, returned after verify and when a
// landlord lists their payment history.
@Builder
public record PaymentResponseDto(
                String paymentId,
                String transactionUuid,
                String gatewayReference,
                PaymentGateway gateway,
                PaymentStatus status,
                BigDecimal amount,
                int featureDays,
                LocalDateTime paidAt,
                Instant createdAt,
                String propertyId,
                String propertyName,
                boolean propertyFeatured,
                LocalDateTime propertyFeaturedUntil) {
}
