package com.room_rental_backend.room_rental_application.interfaces;

import com.room_rental_backend.room_rental_application.enums.NotificationType;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.UserNotificationResponse;
import java.util.List;

// New API: centralized push-notification service. Business services call these
// methods and never talk to Firebase directly. All sends are best-effort.
public interface NotificationService {

    // Associate an FCM token with the given authenticated user (idempotent).
    void registerToken(Users user, String token);

    // Remove one specific token owned by the user.
    void unregisterToken(Users user, String token);

    // Remove every token owned by the user (used on logout).
    void unregisterAllTokens(Users user);

    // Send a notification to all active tokens of a single user.
    void sendToUser(Users user, String title, String body, NotificationType type, String referenceId);

    // Send a notification directly to one token.
    void sendToToken(String token, String title, String body, NotificationType type, String referenceId);

    List<UserNotificationResponse> getNotifications(Users user);
    void markRead(Users user, String notificationId);
}
