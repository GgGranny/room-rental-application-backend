package com.room_rental_backend.room_rental_application.components;

import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import tools.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// New API: publishes real-time roommate/room-status events to the STOMP topic
// /topic/rooms/{roomId}/roommates. Publishing is deferred until AFTER the
// database transaction commits so clients never see uncommitted state.
// Events are UI synchronization only; the REST API response stays authoritative.
@Component
@RequiredArgsConstructor
@Slf4j
public class RoommateEventPublisher {

    public static final String ROOM_STATUS_CHANGED = "ROOM_STATUS_CHANGED";
    public static final String ROOMMATE_REQUEST_CREATED = "ROOMMATE_REQUEST_CREATED";
    public static final String ROOMMATE_REQUEST_ACCEPTED = "ROOMMATE_REQUEST_ACCEPTED";
    public static final String ROOMMATE_REQUEST_REJECTED = "ROOMMATE_REQUEST_REJECTED";
    public static final String ROOMMATE_REQUEST_CANCELLED = "ROOMMATE_REQUEST_CANCELLED";

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public void publish(String roomId, String type, String requestId, String status) {
        // Only safe, non-sensitive fields are broadcast.
        Map<String, Object> payload = Map.of(
                "type", type,
                "roomId", roomId == null ? "" : roomId,
                "requestId", requestId == null ? "" : requestId,
                "status", status == null ? "" : status);

        Runnable send = () -> {
            try {
                messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/roommates",
                        objectMapper.writeValueAsString(payload));
            } catch (Exception ex) {
                // Real-time sync is best-effort; a failed publish never affects business data.
                log.warn("Failed to publish WebSocket event {}: {}", type, ex.getMessage());
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
