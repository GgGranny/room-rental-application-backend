package com.room_rental_backend.room_rental_application.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.room_rental_backend.room_rental_application.enums.PaymentStatus;
import com.room_rental_backend.room_rental_application.models.Landlord;
import com.room_rental_backend.room_rental_application.models.Payment;

// New API: persistence for featured-listing payments.
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByTransactionUuid(String transactionUuid);

    Optional<Payment> findByGatewayReference(String gatewayReference);

    List<Payment> findByLandlordOrderByCreatedAtDesc(Landlord landlord);

    long countByStatus(PaymentStatus status);
}
