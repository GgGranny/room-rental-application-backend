package com.room_rental_backend.room_rental_application.models;

import com.room_rental_backend.room_rental_application.enums.RoommateRequestStatus;
import com.room_rental_backend.room_rental_application.models.base_entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// New API: a request from one tenant to another to share a specific SHARED room.
// Status uses an enum (never a boolean) because PENDING/ACCEPTED/REJECTED/CANCELLED
// are all distinct states the requester must be able to observe.
@Entity
@Table(name = "roommate_requests")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoommateRequest extends BaseEntity {

    // The tenant who sent the request.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private Users requester;

    // The tenant who receives and answers the request.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Users recipient;

    // The shared room both tenants are interested in.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "message", length = 300)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RoommateRequestStatus status = RoommateRequestStatus.PENDING;
}
