package com.room_rental_backend.room_rental_application.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.room_rental_backend.room_rental_application.models.Room;
import com.room_rental_backend.room_rental_application.models.RoommateInterest;
import com.room_rental_backend.room_rental_application.models.Users;

// New API: tenant <-> shared-room interest links used for roommate discovery.
@Repository
public interface RoommateInterestRepository extends JpaRepository<RoommateInterest, String> {

    List<RoommateInterest> findByRoom(Room room);

    Optional<RoommateInterest> findByUserAndRoom(Users user, Room room);

    List<RoommateInterest> findByUserOrderByCreatedAtDesc(Users user);

    boolean existsByUserAndRoom(Users user, Room room);
}
