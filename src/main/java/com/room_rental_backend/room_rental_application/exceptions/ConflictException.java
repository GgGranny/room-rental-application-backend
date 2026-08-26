package com.room_rental_backend.room_rental_application.exceptions;

// New API: 409 Conflict — concurrent/state conflicts such as two tenants trying
// to accept the same roommate request, or acting on an unavailable room.
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
