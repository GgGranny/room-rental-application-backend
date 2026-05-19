package com.room_rental_backend.room_rental_application.exceptions;

public class KycFailedException extends RuntimeException {
    public KycFailedException(String message) {
        super(message);
    }
}
