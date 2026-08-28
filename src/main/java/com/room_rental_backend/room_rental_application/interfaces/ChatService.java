package com.room_rental_backend.room_rental_application.interfaces;

import java.time.Instant;
import java.util.List;

import org.springframework.security.core.Authentication;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.SendMessageRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.ChatMessageResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoommateMatchResponse;

// New API: private roommate chat operations. Access is authorized purely from the
// authenticated principal against the conversation's match participants; no room
// or user ids are ever trusted from the client.
public interface ChatService {

    // Matches (with conversation handle + unread count) for the authenticated user.
    List<RoommateMatchResponse> getMyMatches(Authentication authentication);

    // Paged message history (oldest-first). Participants may always READ, even
    // after the match has ENDED. `before` is an optional createdAt cursor.
    List<ChatMessageResponse> getMessages(String conversationId, Instant before, Integer limit,
            Authentication authentication);

    // Send a message. Sender = authenticated user; match must be ACTIVE.
    ChatMessageResponse sendMessage(String conversationId, SendMessageRequest request,
            Authentication authentication);

    // Mark every message the peer sent me in this conversation as read.
    void markRead(String conversationId, Authentication authentication);
}
