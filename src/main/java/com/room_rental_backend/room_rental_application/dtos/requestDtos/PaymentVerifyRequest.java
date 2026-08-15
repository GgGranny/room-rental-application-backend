package com.room_rental_backend.room_rental_application.dtos.requestDtos;

// New API: verify a payment after the gateway redirects back. Provide the
// transactionUuid we issued at initiate time (preferred). pidx is accepted as a
// fallback for Khalti callbacks that only echo the pidx.
public record PaymentVerifyRequest(
                String transactionUuid,
                String pidx) {
}
