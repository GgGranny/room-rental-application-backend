package com.room_rental_backend.room_rental_application.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.room_rental_backend.room_rental_application.models.RoommateProfile;
import com.room_rental_backend.room_rental_application.models.Users;

// New API: roommate profile persistence, one per user.
@Repository
public interface RoommateProfileRepository extends JpaRepository<RoommateProfile, String> {

    Optional<RoommateProfile> findByUser(Users user);

    boolean existsByUser(Users user);
}
