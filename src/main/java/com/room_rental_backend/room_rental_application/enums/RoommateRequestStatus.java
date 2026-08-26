package com.room_rental_backend.room_rental_application.enums;

// New API: lifecycle of a roommate request between two tenants for one shared room.
public enum RoommateRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED
}
