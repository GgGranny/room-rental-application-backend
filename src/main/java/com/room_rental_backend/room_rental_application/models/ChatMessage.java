package com.room_rental_backend.room_rental_application.models;

import java.time.Instant;

import com.room_rental_backend.room_rental_application.models.base_entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// New API: a single message inside a private roommate conversation. The sender is
// always resolved from the authenticated principal in the service (never trusted
// from the client), so a tenant cannot impersonate the other participant.
// createdAt (from BaseEntity) is the sent time; readAt is null until the peer reads.
@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    // The authenticated author of the message.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private Users sender;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    // Set when the OTHER participant reads the message; null while unread.
    @Column(name = "read_at")
    private Instant readAt;
}
