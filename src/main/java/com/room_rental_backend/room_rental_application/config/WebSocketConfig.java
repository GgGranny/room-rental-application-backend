package com.room_rental_backend.room_rental_application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// New API: minimal STOMP-over-WebSocket setup for real-time UI synchronization.
// The broker only broadcasts safe, room-scoped events to /topic/rooms/{roomId}/roommates.
// WebSocket is NEVER a security boundary — every state change goes through the
// authenticated REST APIs which re-check KYC, ownership, room status and capacity.
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Broadcast-only topics; clients never send application messages.
        registry.enableSimpleBroker("/topic");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:3000", "https://localhost:3000");
    }
}
