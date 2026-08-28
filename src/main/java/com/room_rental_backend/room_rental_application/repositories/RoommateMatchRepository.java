package com.room_rental_backend.room_rental_application.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.room_rental_backend.room_rental_application.enums.MatchStatus;
import com.room_rental_backend.room_rental_application.models.Room;
import com.room_rental_backend.room_rental_application.models.RoommateMatch;
import com.room_rental_backend.room_rental_application.models.Users;

// New API: persistence for confirmed roommate matches. The (room, tenantOne,
// tenantTwo) unique constraint on the entity is the last line of defence against
// duplicate matches; these finders drive idempotent creation and "my matches".
@Repository
public interface RoommateMatchRepository extends JpaRepository<RoommateMatch, String> {

    // Idempotent match lookup for the NORMALIZED tenant pair on one room.
    Optional<RoommateMatch> findByRoomAndTenantOneAndTenantTwoAndStatus(
            Room room, Users tenantOne, Users tenantTwo, MatchStatus status);

    // "My matches": rows where the authenticated user is either participant.
    // Callers pass the same user for both arguments.
    List<RoommateMatch> findByTenantOneOrTenantTwoOrderByCreatedAtDesc(Users tenantOne, Users tenantTwo);
}
