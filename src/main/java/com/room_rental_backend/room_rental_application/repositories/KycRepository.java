package com.room_rental_backend.room_rental_application.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.room_rental_backend.room_rental_application.models.Kyc;

public interface KycRepository extends JpaRepository<Kyc, Integer> {

    boolean existsByUserId(String userId);

    Optional<Kyc> findByUserId(String customerId);
}
