package com.room_rental_backend.room_rental_application.controllers;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.SendMessageRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.ChatMessageResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoommateMatchResponse;
import com.room_rental_backend.room_rental_application.interfaces.ChatService;
import com.room_rental_backend.room_rental_application.responseHandler.ApiResponse;
import com.room_rental_backend.room_rental_application.responseHandler.GlobalResponseHandler;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// New API: private roommate chat endpoints. Nested under /api/v1/roommates so the
// existing hasRole("USER") path rule already applies (no SpringSecurity change).
// The service re-checks conversation participation on EVERY call, so a tenant can
// only ever see or write to their own matches.
@RestController
@RequestMapping("/api/v1/roommates")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // Tenant: my roommate matches, each with its conversation handle + unread count.
    @GetMapping("/matches")
    public ResponseEntity<ApiResponse<List<RoommateMatchResponse>>> getMyMatches(Authentication authentication) {
        return GlobalResponseHandler.success("Roommate matches fetched successfully",
                chatService.getMyMatches(authentication), HttpStatus.OK);
    }

    // Tenant (participant only): paged message history, oldest-first.
    // `before` is an optional ISO-8601 createdAt cursor for loading older messages.
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            @PathVariable("conversationId") String conversationId,
            @RequestParam(value = "before", required = false) String before,
            @RequestParam(value = "limit", required = false) Integer limit,
            Authentication authentication) {
        Instant beforeCursor = parseCursor(before);
        return GlobalResponseHandler.success("Messages fetched successfully",
                chatService.getMessages(conversationId, beforeCursor, limit, authentication), HttpStatus.OK);
    }

    // Tenant (participant only): send a message. Sender comes from the JWT.
    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @PathVariable("conversationId") String conversationId,
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication) {
        return GlobalResponseHandler.success("Message sent successfully",
                chatService.sendMessage(conversationId, request, authentication), HttpStatus.CREATED);
    }

    // Tenant (participant only): mark the peer's messages in this conversation read.
    @PatchMapping("/conversations/{conversationId}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @PathVariable("conversationId") String conversationId,
            Authentication authentication) {
        chatService.markRead(conversationId, authentication);
        return GlobalResponseHandler.success("Conversation marked as read", null, HttpStatus.OK);
    }

    // A malformed cursor is a client error (400), not a silent fallback.
    private Instant parseCursor(String before) {
        if (before == null || before.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(before);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid 'before' cursor; expected an ISO-8601 instant");
        }
    }
}
