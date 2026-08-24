package com.room_rental_backend.room_rental_application.exceptions;

// New API: thrown by the FCM sender when Firebase reports a token as
// unregistered/invalid so the notification service can deactivate it.
public class FcmUnregisteredTokenException extends RuntimeException {
    public FcmUnregisteredTokenException(String message) {
        super(message);
    }
}