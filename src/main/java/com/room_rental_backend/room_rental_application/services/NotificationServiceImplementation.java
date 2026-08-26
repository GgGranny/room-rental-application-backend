package com.room_rental_backend.room_rental_application.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.room_rental_backend.room_rental_application.enums.NotificationType;
import com.room_rental_backend.room_rental_application.exceptions.FcmUnregisteredTokenException;
import com.room_rental_backend.room_rental_application.interfaces.NotificationService;
import com.room_rental_backend.room_rental_application.models.DeviceToken;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.repositories.DeviceTokenRepository;
import com.room_rental_backend.room_rental_application.repositories.UserNotificationRepository;
import com.room_rental_backend.room_rental_application.models.UserNotification;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.UserNotificationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// New API: centralized notification orchestration. FCM failures are logged and
// swallowed so a failed push never rolls back the triggering business operation.
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImplementation implements NotificationService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final FcmSender fcmSender;
    private final UserNotificationRepository userNotificationRepository;

    @Transactional
    @Override
    public void registerToken(Users user, String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("FCM token is required");
        }

        // Upsert: the same token may re-register (re-login/token refresh).
        deviceTokenRepository.findByToken(token).ifPresentOrElse(existing -> {
            // If the token moved to a different user (rare), reassign ownership.
            if (!existing.getUser().getId().equals(user.getId())) {
                existing.setUser(user);
            }
            existing.setActive(true);
            deviceTokenRepository.save(existing);
        }, () -> {
            deviceTokenRepository.save(DeviceToken.builder()
                    .user(user)
                    .token(token)
                    .active(true)
                    .build());
        });
    }

    @Transactional
    @Override
    public void unregisterToken(Users user, String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        deviceTokenRepository.findByUserAndToken(user, token)
                .ifPresent(deviceTokenRepository::delete);
    }

    @Transactional
    @Override
    public void unregisterAllTokens(Users user) {
        deviceTokenRepository.deleteByUser(user);
    }

    @Override
    public void sendToUser(Users user, String title, String body, NotificationType type, String referenceId) {
        if (user == null) {
            return;
        }
        userNotificationRepository.save(UserNotification.builder().user(user).title(title).body(body)
                .type(type).referenceId(referenceId).build());
        List<DeviceToken> tokens = deviceTokenRepository.findByUserAndActiveTrue(user);
        if (tokens.isEmpty()) {
            return;
        }
        Map<String, String> data = buildData(type, referenceId);
        for (DeviceToken deviceToken : tokens) {
            send(deviceToken, title, body, data);
        }
    }

    @Override
    public void sendToToken(String token, String title, String body, NotificationType type, String referenceId) {
        if (token == null || token.isBlank()) {
            return;
        }
        send(DeviceToken.builder().token(token).active(true).build(), title, body, buildData(type, referenceId));
    }

    @Override
    public List<UserNotificationResponse> getNotifications(Users user) {
        return userNotificationRepository.findTop50ByUserOrderByCreatedAtDesc(user).stream()
                .map(item -> new UserNotificationResponse(item.getId(), item.getTitle(), item.getBody(), item.getType(),
                        item.getReferenceId(), item.isRead(), item.getCreatedAt()))
                .toList();
    }

    @Override
    public long getUnreadCount(Users user) {
        return userNotificationRepository.countByUserAndReadFalse(user);
    }

    @Transactional
    @Override
    public void markRead(Users user, String notificationId) {
        UserNotification notification = userNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Notification does not belong to the authenticated user");
        }
        notification.setRead(true);
        userNotificationRepository.save(notification);
    }

    @Transactional
    @Override
    public void markAllRead(Users user) {
        List<UserNotification> unread = userNotificationRepository.findByUserAndReadFalse(user);
        unread.forEach(item -> item.setRead(true));
        userNotificationRepository.saveAll(unread);
    }

    private void send(DeviceToken deviceToken, String title, String body, Map<String, String> data) {
        try {
            fcmSender.send(deviceToken.getToken(), title, body, data);
        } catch (FcmUnregisteredTokenException e) {
            // Deactivate the stale token instead of letting it accumulate.
            if (deviceToken.getId() != null) {
                deviceToken.setActive(false);
                deviceTokenRepository.save(deviceToken);
                log.info("Deactivated invalid FCM token for user {}",
                        deviceToken.getUser() != null ? deviceToken.getUser().getId() : "unknown");
            } else {
                log.info("Ignoring invalid FCM token (not persisted): {}", e.getMessage());
            }
        } catch (Exception e) {
            log.warn("Push notification delivery failed: {}", e.getMessage());
        }
    }

    private Map<String, String> buildData(NotificationType type, String referenceId) {
        Map<String, String> data = new HashMap<>();
        data.put("type", type.name());
        data.put("action", type.getAction());
        data.put("referenceId", referenceId == null ? "" : referenceId);
        return data;
    }
}
