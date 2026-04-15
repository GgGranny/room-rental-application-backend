package com.room_rental_backend.room_rental_application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.room_rental_backend.room_rental_application.models.RoomFacilities;

@Repository
public interface RoomFacilitiesRepository extends JpaRepository<RoomFacilities, String> {

}