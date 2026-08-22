package com.room_rental_backend.room_rental_application.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.room_rental_backend.room_rental_application.models.UserNotification;
import com.room_rental_backend.room_rental_application.models.Users;

public interface UserNotificationRepository extends JpaRepository<UserNotification, String> {
    List<UserNotification> findTop50ByUserOrderByCreatedAtDesc(Users user);
}
