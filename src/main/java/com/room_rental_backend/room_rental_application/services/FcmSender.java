package com.room_rental_backend.room_rental_application.services;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.room_rental_backend.room_rental_application.exceptions.FcmUnregisteredTokenException;

// Low-level FCM wrapper. Business logic should go through NotificationService,
// which decides recipients and handles token lifecycle.
@Service
public class FcmSender {

    private static final Logger log = LoggerFactory.getLogger(FcmSender.class);

    public void send(String deviceToken, String title, String body) {
        send(deviceToken, title, body, Map.of());
    }

    public void send(String deviceToken, String title, String body, Map<String, String> data) {
        Message message = Message.builder()
                .setToken(deviceToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data)
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            // FCM says this token is no longer valid (uninstalled/revoked/permission
            // cleared). Signal the caller so it can deactivate the stored token.
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                    || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                throw new FcmUnregisteredTokenException("FCM token is no longer registered");
            }
            // Other Firebase errors must never break the triggering business operation.
            log.warn("Failed to send FCM message: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Failed to send FCM message: {}", e.getMessage());
        }
    }
}