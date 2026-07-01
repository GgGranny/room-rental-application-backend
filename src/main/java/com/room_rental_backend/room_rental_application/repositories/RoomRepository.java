package com.room_rental_backend.room_rental_application.repositories;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.room_rental_backend.room_rental_application.enums.RoomStatus;
import com.room_rental_backend.room_rental_application.models.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, String>, JpaSpecificationExecutor<Room> {

    List<Room> findTop12ByStatusOrderByCreatedAtDesc(RoomStatus status);

    List<Room> findByIdIn(Collection<String> ids);
}
