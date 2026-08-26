package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import java.math.BigDecimal;
import java.util.List;

import com.room_rental_backend.room_rental_application.enums.RoomStatus;
import com.room_rental_backend.room_rental_application.enums.RoomType;
import com.room_rental_backend.room_rental_application.enums.RoommateRequestStatus;

// New API: everything the shared-room roommate map needs in one call. The map
// plots every marker around the ROOM's own coordinates — tenant home locations
// are never stored or exposed.
public record RoommateMapResponse(
        RoomSummary room,
        List<Opportunity> opportunities) {

    public record RoomSummary(
            String roomId,
            String roomTitle,
            BigDecimal price,
            String location,
            Double latitude,
            Double longitude,
            RoomStatus status,
            RoomType sharingType) {
    }

    // One marker card. pendingIncomingRequestId is set when this user has a
    // PENDING request waiting for the current viewer, so the card offers Accept.
    // myRequestStatus mirrors the viewer's own outgoing request state toward this user.
    public record Opportunity(
            String userId,
            String name,
            String profilePictureUrl,
            BigDecimal budget,
            String bio,
            int compatibilityScore,
            RoommateRequestStatus myRequestStatus,
            String pendingIncomingRequestId) {
    }
}
