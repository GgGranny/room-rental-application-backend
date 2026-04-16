package com.room_rental_backend.room_rental_application.exceptions;

public class PhoneNumberAlreadyExists extends RuntimeException {
    public PhoneNumberAlreadyExists(String message) {
        super(message);
    }

}
