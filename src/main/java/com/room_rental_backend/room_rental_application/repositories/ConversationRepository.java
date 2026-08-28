package com.room_rental_backend.room_rental_application.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.room_rental_backend.room_rental_application.models.Conversation;
import com.room_rental_backend.room_rental_application.models.RoommateMatch;

// New API: persistence for the one-to-one conversation attached to a match.
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    Optional<Conversation> findByMatch(RoommateMatch match);
}
