package com.room_rental_backend.room_rental_application.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.room_rental_backend.room_rental_application.models.Landlord;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.models.ViewingSchedule;

// New API: viewing-request persistence, scoped by tenant or by owning landlord.
@Repository
public interface ViewingScheduleRepository extends JpaRepository<ViewingSchedule, String> {

    List<ViewingSchedule> findByTenantOrderByScheduledAtDesc(Users tenant);

    List<ViewingSchedule> findByLandlordOrderByScheduledAtDesc(Landlord landlord);

    List<ViewingSchedule> findByRoomAndScheduledAtAndStatusAndIdNot(
            com.room_rental_backend.room_rental_application.models.Room room,
            java.time.LocalDateTime scheduledAt,
            com.room_rental_backend.room_rental_application.enums.ScheduleStatus status,
            String id);

    boolean existsByRoomAndTenantAndScheduledAtAndStatusIn(
            com.room_rental_backend.room_rental_application.models.Room room,
            Users tenant,
            java.time.LocalDateTime scheduledAt,
            List<com.room_rental_backend.room_rental_application.enums.ScheduleStatus> statuses);
}
