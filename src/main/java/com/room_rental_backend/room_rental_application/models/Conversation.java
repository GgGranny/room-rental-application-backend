package com.room_rental_backend.room_rental_application.models;

import com.room_rental_backend.room_rental_application.models.base_entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// New API: the private conversation that belongs to exactly one roommate match.
// One-to-one with RoommateMatch so chat access can be authorized purely through
// the match participants. The conversation id is a server-generated UUID and is
// the only handle the frontend uses to fetch/send messages or subscribe to the
// real-time topic /topic/conversations/{id}.
@Entity
@Table(name = "conversations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Conversation extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private RoommateMatch match;
}
