package com.room_rental_backend.room_rental_application.dtos.requestDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// New API: payload for sending a private chat message. ONLY the content is
// accepted from the client — the sender is always resolved from the JWT in the
// service, so a client cannot set a senderId and impersonate the other tenant.
public record SendMessageRequest(
        @NotBlank(message = "Message cannot be empty")
        @Size(max = 2000, message = "Message cannot exceed 2000 characters")
        String content) {
}
