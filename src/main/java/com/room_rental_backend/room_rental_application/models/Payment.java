package com.room_rental_backend.room_rental_application.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.room_rental_backend.room_rental_application.models.base_entity.BaseEntity;
import com.room_rental_backend.room_rental_application.enums.PaymentGateway;
import com.room_rental_backend.room_rental_application.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// New API: a featured-listing payment attempt against a payment gateway (sandbox).
@Entity
@Table(name = "payments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "landlord_id", nullable = false)
    private Landlord landlord;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "gateway", nullable = false)
    private PaymentGateway gateway;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    // Our own idempotency key: eSewa transaction_uuid / Khalti purchase_order_id.
    @Column(name = "transaction_uuid", unique = true, nullable = false)
    private String transactionUuid;

    // Gateway-side reference once known: eSewa transaction_code / Khalti pidx.
    @Column(name = "gateway_reference")
    private String gatewayReference;

    // How many days of featuring this payment grants.
    @Column(name = "feature_days")
    private int featureDays;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
