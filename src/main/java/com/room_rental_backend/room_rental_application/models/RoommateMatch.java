package com.room_rental_backend.room_rental_application.models;

import com.room_rental_backend.room_rental_application.enums.MatchStatus;
import com.room_rental_backend.room_rental_application.models.base_entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// New API: a confirmed roommate relationship created when a PENDING roommate
// request is accepted. This is the authorization anchor for the private chat:
// only tenantOne / tenantTwo of a match may access its conversation.
//
// The tenant pair is stored NORMALIZED (smaller UUID -> tenantOne) so the unique
// constraint on (room, tenantOne, tenantTwo) rejects duplicate matches regardless
// of who accepted whom. Combined with the pessimistic locks already held in
// RoommateServiceImplementation.acceptRequest, this prevents duplicate active
// matches for the same two tenants on the same shared room.
@Entity
@Table(name = "roommate_matches", uniqueConstraints = @UniqueConstraint(name = "uk_match_room_tenants", columnNames = {
        "room_id", "tenant_one_id", "tenant_two_id" }))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoommateMatch extends BaseEntity {

    // The shared room this match is anchored to.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // Normalized pair: tenantOne always holds the lexicographically smaller id.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_one_id", nullable = false)
    private Users tenantOne;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_two_id", nullable = false)
    private Users tenantTwo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private MatchStatus status = MatchStatus.ACTIVE;

    // The request whose acceptance created this match (audit / traceability).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_request_id")
    private RoommateRequest sourceRequest;
}
