package com.room_rental_backend.room_rental_application.events;

public record UserRegisterEvent(String email, String activationUrl) {
}
