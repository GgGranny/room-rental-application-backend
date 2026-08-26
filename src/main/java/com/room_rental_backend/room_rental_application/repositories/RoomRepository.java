package com.room_rental_backend.room_rental_application.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.room_rental_backend.room_rental_application.enums.RoomStatus;
import com.room_rental_backend.room_rental_application.models.Room;

import jakarta.persistence.LockModeType;

@Repository
public interface RoomRepository extends JpaRepository<Room, String>, JpaSpecificationExecutor<Room> {

    List<Room> findTop12ByStatusOrderByCreatedAtDesc(RoomStatus status);

    List<Room> findByIdIn(Collection<String> ids);

    // New API: pessimistic lock so concurrent roommate request creation/acceptance
    // serializes against room availability changes for the same room.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") String id);

    // New API: "Find Rooms Near You" backend search. Filters at the DATABASE level
    // (never in the frontend) using the Haversine great-circle formula so only
    // rooms within :radiusKm are returned, closest first. Business visibility rules
    // are enforced here: only AVAILABLE, non-deleted rooms that actually have
    // coordinates can appear on the map. The acos() argument is clamped to
    // [-1, 1] to avoid floating-point domain errors. Each row is [id, distanceKm].
    @Query(value = """
            SELECT r.id AS id,
                   (6371 * acos(LEAST(1, GREATEST(-1,
                        cos(radians(:lat)) * cos(radians(r.latitude)) *
                        cos(radians(r.longitude) - radians(:lng)) +
                        sin(radians(:lat)) * sin(radians(r.latitude))
                   )))) AS distance_km
            FROM rooms r
            WHERE r.status = 'AVAILABLE'
              AND r.is_deleted = false
              AND r.latitude IS NOT NULL
              AND r.longitude IS NOT NULL
              AND (6371 * acos(LEAST(1, GREATEST(-1,
                        cos(radians(:lat)) * cos(radians(r.latitude)) *
                        cos(radians(r.longitude) - radians(:lng)) +
                        sin(radians(:lat)) * sin(radians(r.latitude))
                   )))) <= :radiusKm
            ORDER BY distance_km ASC
            """, nativeQuery = true)
    List<Object[]> findNearbyAvailableRooms(@Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm);
}
