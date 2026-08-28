package com.room_rental_backend.room_rental_application.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.room_rental_backend.room_rental_application.models.ChatMessage;
import com.room_rental_backend.room_rental_application.models.Conversation;
import com.room_rental_backend.room_rental_application.models.Users;

// New API: private chat message persistence. History is paged newest-first and
// reversed for display in the service; unread counts and bulk mark-read power
// the read-receipt feature.
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    // Latest page of a conversation (newest first).
    List<ChatMessage> findByConversationOrderByCreatedAtDesc(Conversation conversation, Pageable pageable);

    // Older page for cursor pagination: messages sent before the given instant.
    List<ChatMessage> findByConversationAndCreatedAtBeforeOrderByCreatedAtDesc(
            Conversation conversation, Instant before, Pageable pageable);

    // Unread = messages in this conversation NOT sent by me that I haven't read.
    long countByConversationAndSenderNotAndReadAtIsNull(Conversation conversation, Users sender);

    // Bulk mark-read: stamp readAt on every unread message the peer sent to me.
    // Returns the number of rows updated so the service only publishes a
    // MESSAGE_READ event when something actually changed.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ChatMessage m set m.readAt = :now "
            + "where m.conversation = :conversation and m.sender <> :me and m.readAt is null")
    int markConversationRead(@Param("conversation") Conversation conversation,
            @Param("me") Users me, @Param("now") Instant now);
}
