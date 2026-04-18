package com.room_rental_backend.room_rental_application.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendOtp(String to, String Subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply");
        message.setTo(to);
        message.setSubject(Subject);
        message.setText(body);
        mailSender.send(message);
    }
}
