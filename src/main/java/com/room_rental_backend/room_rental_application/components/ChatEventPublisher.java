package com.room_rental_backend.room_rental_application.components;

import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import tools.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// New API: publishes real-time chat events to the STOMP topic
// /topic/conversations/{conversationId}. Mirrors RoommateEventPublisher:
//  - Publishing is deferred until AFTER the DB transaction commits so clients
//    never observe a message that later rolls back.
//  - Only lightweight, non-sensitive SIGNALS are broadcast (type + ids). Message
//    CONTENT is never put on the broker; clients refetch the message via the
//    authenticated REST API (which re-checks participation), keeping REST
//    authoritative and honouring "never send private content over WebSocket".
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatEventPublisher {

    public static final String CHAT_MESSAGE = "CHAT_MESSAGE";
    public static final String MESSAGE_READ = "MESSAGE_READ";

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    // A new message arrived: signal only (id + sender), no content.
    public void publishMessage(String conversationId, String messageId, String senderId) {
        publish(conversationId, Map.of(
                "type", CHAT_MESSAGE,
                "conversationId", conversationId == null ? "" : conversationId,
                "messageId", messageId == null ? "" : messageId,
                "senderId", senderId == null ? "" : senderId));
    }

    // The peer read messages: lets the sender's UI refresh read receipts.
    public void publishRead(String conversationId, String readerId) {
        publish(conversationId, Map.of(
                "type", MESSAGE_READ,
                "conversationId", conversationId == null ? "" : conversationId,
                "readerId", readerId == null ? "" : readerId));
    }

    private void publish(String conversationId, Map<String, Object> payload) {
        Runnable send = () -> {
            try {
                messagingTemplate.convertAndSend("/topic/conversations/" + conversationId,
                        objectMapper.writeValueAsString(payload));
            } catch (Exception ex) {
                // Real-time sync is best-effort; a failed publish never affects business data.
                log.warn("Failed to publish chat event {}: {}", payload.get("type"), ex.getMessage());
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send.run();
                }
            });
        } else {
            send.run();
        }
    }
}
