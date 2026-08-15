package com.room_rental_backend.room_rental_application.mappers;

import org.springframework.stereotype.Component;

import com.room_rental_backend.room_rental_application.dtos.responseDtos.PaymentResponseDto;
import com.room_rental_backend.room_rental_application.models.Payment;
import com.room_rental_backend.room_rental_application.models.Property;

// New API: builds the landlord-facing summary of a featuring payment.
@Component
public class PaymentMapper {

    public PaymentResponseDto toResponse(Payment payment) {
        Property property = payment.getProperty();
        return PaymentResponseDto.builder()
                .paymentId(payment.getId())
                .transactionUuid(payment.getTransactionUuid())
                .gatewayReference(payment.getGatewayReference())
                .gateway(payment.getGateway())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .featureDays(payment.getFeatureDays())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .propertyId(property == null ? null : property.getId())
                .propertyName(property == null ? null : property.getPropertyName())
                // .propertyFeatured(property != null && property.isFeatured())
                // .propertyFeaturedUntil(property == null ? null : property.getFeaturedUntil())
                .build();
    }
}
