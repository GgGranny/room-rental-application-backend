package com.room_rental_backend.room_rental_application.exceptions;

public class TokenNotFoundException extends RuntimeException {
    public TokenNotFoundException(String mesage) {
        super(mesage);
    }
}
