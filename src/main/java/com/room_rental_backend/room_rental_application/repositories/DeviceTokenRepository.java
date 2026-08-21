package com.room_rental_backend.room_rental_application.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.room_rental_backend.room_rental_application.models.DeviceToken;
import com.room_rental_backend.room_rental_application.models.Users;

// New API: persistence for FCM device/browser tokens keyed by owning user.
@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, String> {

    Optional<DeviceToken> findByToken(String token);

    Optional<DeviceToken> findByUserAndToken(Users user, String token);

    List<DeviceToken> findByUserAndActiveTrue(Users user);

    List<DeviceToken> findByUser(Users user);

    void deleteByUser(Users user);

    long countByUserAndActiveTrue(Users user);
}