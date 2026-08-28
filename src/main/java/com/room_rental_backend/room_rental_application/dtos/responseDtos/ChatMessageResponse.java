package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import java.time.Instant;

// New API: a single chat message. "mine" is computed server-side from the
// authenticated user so the client never has to compare ids to render bubbles.
public record ChatMessageResponse(
        String id,
        String conversationId,
        String senderId,
        String senderName,
        String content,
        boolean mine,
        Instant createdAt,
        Instant readAt) {
}
