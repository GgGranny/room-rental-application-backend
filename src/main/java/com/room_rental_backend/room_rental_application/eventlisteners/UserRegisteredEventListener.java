package com.room_rental_backend.room_rental_application.eventlisteners;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.room_rental_backend.room_rental_application.events.UserRegisterEvent;
import com.room_rental_backend.room_rental_application.services.MailService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRegisteredEventListener {

    private final MailService mailService;

    @EventListener
    public void handleUserRegistered(UserRegisterEvent event) {
        try {
            mailService.sendOtp(event.email(),
                    "Account Registration",
                    event.activationUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
