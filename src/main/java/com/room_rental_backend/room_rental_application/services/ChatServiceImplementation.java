package com.room_rental_backend.room_rental_application.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.room_rental_backend.room_rental_application.components.ChatEventPublisher;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.SendMessageRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.ChatMessageResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoommateMatchResponse;
import com.room_rental_backend.room_rental_application.enums.MatchStatus;
import com.room_rental_backend.room_rental_application.enums.NotificationType;
import com.room_rental_backend.room_rental_application.exceptions.ConflictException;
import com.room_rental_backend.room_rental_application.exceptions.ForbiddenException;
import com.room_rental_backend.room_rental_application.exceptions.UnauthorizedException;
import com.room_rental_backend.room_rental_application.exceptions.UserNotFoundException;
import com.room_rental_backend.room_rental_application.interfaces.ChatService;
import com.room_rental_backend.room_rental_application.interfaces.NotificationService;
import com.room_rental_backend.room_rental_application.models.ChatMessage;
import com.room_rental_backend.room_rental_application.models.Conversation;
import com.room_rental_backend.room_rental_application.models.Room;
import com.room_rental_backend.room_rental_application.models.RoommateMatch;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.repositories.ChatMessageRepository;
import com.room_rental_backend.room_rental_application.repositories.ConversationRepository;
import com.room_rental_backend.room_rental_application.repositories.RoommateMatchRepository;
import com.room_rental_backend.room_rental_application.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// New API: private roommate chat. Security model:
//  - The sender/reader is ALWAYS resolved from the authenticated principal
//    (resolveUser); no senderId/userId is ever read from the request body, so a
//    tenant cannot impersonate another.
//  - Every operation re-checks that the caller is one of the two tenants of the
//    conversation's match (requireParticipant); anyone else gets HTTP 403.
//  - Reading history stays allowed after a match ENDS; only SENDING requires an
//    ACTIVE match (else HTTP 409).
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImplementation implements ChatService {

    private static final int DEFAULT_PAGE_SIZE = 30;
    private static final int MAX_PAGE_SIZE = 100;

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final RoommateMatchRepository matchRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ChatEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    @Override
    public List<RoommateMatchResponse> getMyMatches(Authentication authentication) {
        Users me = resolveUser(authentication);
        return matchRepository.findByTenantOneOrTenantTwoOrderByCreatedAtDesc(me, me).stream()
                .map(match -> toMatchResponse(match, me))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ChatMessageResponse> getMessages(String conversationId, Instant before, Integer limit,
            Authentication authentication) {
        Users me = resolveUser(authentication);
        Conversation conversation = getConversation(conversationId);
        // Participants may always READ, even if the match has ENDED (spec §29).
        requireParticipant(conversation, me);

        Pageable pageable = PageRequest.of(0, clampLimit(limit));
        List<ChatMessage> page = before == null
                ? messageRepository.findByConversationOrderByCreatedAtDesc(conversation, pageable)
                : messageRepository.findByConversationAndCreatedAtBeforeOrderByCreatedAtDesc(
                        conversation, before, pageable);

        // Repo returns newest-first for the cursor; reverse to oldest-first for display.
        List<ChatMessageResponse> ordered = new ArrayList<>(page.size());
        for (int i = page.size() - 1; i >= 0; i--) {
            ordered.add(toMessageResponse(page.get(i), me));
        }
        return ordered;
    }

    @Transactional
    @Override
    public ChatMessageResponse sendMessage(String conversationId, SendMessageRequest request,
            Authentication authentication) {
        Users me = resolveUser(authentication);
        Conversation conversation = getConversation(conversationId);
        requireParticipant(conversation, me);

        RoommateMatch match = conversation.getMatch();
        if (match.getStatus() != MatchStatus.ACTIVE) {
            throw new ConflictException("This roommate chat is no longer active.");
        }

        // Sender is the authenticated user — NEVER trusted from the payload.
        ChatMessage saved = messageRepository.save(ChatMessage.builder()
                .conversation(conversation)
                .sender(me)
                .content(request.content().trim())
                .build());

        // Offline delivery: always persist + FCM-push to the peer (spec §27).
        Users peer = peerOf(match, me);
        notificationService.sendToUser(peer, displayName(me) + " sent you a message",
                preview(saved.getContent()), NotificationType.NEW_MESSAGE, conversation.getId());

        // Real-time SIGNAL after commit (no content on the broker).
        eventPublisher.publishMessage(conversation.getId(), saved.getId(), me.getId());

        return toMessageResponse(saved, me);
    }

    @Transactional
    @Override
    public void markRead(String conversationId, Authentication authentication) {
        Users me = resolveUser(authentication);
        Conversation conversation = getConversation(conversationId);
        requireParticipant(conversation, me);

        int updated = messageRepository.markConversationRead(conversation, me, Instant.now());
        if (updated > 0) {
            // Let the sender's UI refresh its read receipts.
            eventPublisher.publishRead(conversation.getId(), me.getId());
        }
    }

    // --- helpers -----------------------------------------------------------

    private Users resolveUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));
    }

    private Conversation getConversation(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found for id: " + conversationId));
    }

    // Chat authorization: the caller MUST be one of the match's two tenants.
    private void requireParticipant(Conversation conversation, Users me) {
        RoommateMatch match = conversation.getMatch();
        String meId = me.getId();
        boolean participant = meId.equals(match.getTenantOne().getId())
                || meId.equals(match.getTenantTwo().getId());
        if (!participant) {
            throw new ForbiddenException("You do not have access to this conversation");
        }
    }

    private Users peerOf(RoommateMatch match, Users me) {
        return me.getId().equals(match.getTenantOne().getId()) ? match.getTenantTwo() : match.getTenantOne();
    }

    private int clampLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(limit, MAX_PAGE_SIZE);
    }

    private RoommateMatchResponse toMatchResponse(RoommateMatch match, Users me) {
        Conversation conversation = conversationRepository.findByMatch(match).orElse(null);
        Users peer = peerOf(match, me);
        Room room = match.getRoom();
        long unread = conversation == null ? 0L
                : messageRepository.countByConversationAndSenderNotAndReadAtIsNull(conversation, me);
        return new RoommateMatchResponse(
                match.getId(),
                conversation == null ? null : conversation.getId(),
                room.getId(),
                room.getRoomTitle(),
                match.getStatus(),
                new RoommateMatchResponse.UserSummary(peer.getId(), displayName(peer), peer.getProfilePictureUrl()),
                unread,
                match.getCreatedAt());
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message, Users me) {
        Users sender = message.getSender();
        return new ChatMessageResponse(
                message.getId(),
                message.getConversation().getId(),
                sender.getId(),
                displayName(sender),
                message.getContent(),
                sender.getId().equals(me.getId()),
                message.getCreatedAt(),
                message.getReadAt());
    }

    private String displayName(Users user) {
        if (user == null) {
            return "A tenant";
        }
        String first = user.getFname() == null ? "" : user.getFname();
        String last = user.getLname() == null ? "" : user.getLname();
        String name = (first + " " + last).trim();
        return name.isEmpty() ? user.getEmail() : name;
    }

    // Short preview for the push/in-app notification body; full content stays in chat.
    private String preview(String content) {
        String trimmed = content == null ? "" : content.trim();
        return trimmed.length() <= 120 ? trimmed : trimmed.substring(0, 120) + "…";
    }
}
