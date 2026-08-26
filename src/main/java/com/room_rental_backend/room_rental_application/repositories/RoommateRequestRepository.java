package com.room_rental_backend.room_rental_application.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.room_rental_backend.room_rental_application.enums.RoommateRequestStatus;
import com.room_rental_backend.room_rental_application.models.Room;
import com.room_rental_backend.room_rental_application.models.RoommateRequest;
import com.room_rental_backend.room_rental_application.models.Users;

import jakarta.persistence.LockModeType;

// New API: roommate request persistence scoped by requester or recipient.
@Repository
public interface RoommateRequestRepository extends JpaRepository<RoommateRequest, String> {

    // Duplicate prevention: only one ACTIVE request per pair of tenants per room.
    boolean existsByRequesterAndRecipientAndRoomAndStatusIn(
            Users requester, Users recipient, Room room,
            Collection<RoommateRequestStatus> statuses);

    List<RoommateRequest> findByRequesterOrderByCreatedAtDesc(Users requester);

    List<RoommateRequest> findByRecipientOrderByCreatedAtDesc(Users recipient);

    // New API: pessimistic lock used by acceptance so two concurrent accepts
    // cannot both pass the PENDING check — the loser gets HTTP 409.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RoommateRequest r where r.id = :id")
    Optional<RoommateRequest> findByIdForUpdate(@Param("id") String id);

    long countByRoomAndStatus(Room room, RoommateRequestStatus status);

    List<RoommateRequest> findByRoomAndStatus(Room room, RoommateRequestStatus status);

    // History between a pair for one room (latest first) to expose request state on map cards.
    List<RoommateRequest> findByRequesterAndRecipientAndRoomOrderByCreatedAtDesc(
            Users requester, Users recipient, Room room);
}
