package com.room_rental_backend.room_rental_application.interfaces;

import java.util.List;

import com.room_rental_backend.room_rental_application.models.Users;

public interface UserService {
    List<Users> getAllUsers();

    void deleteUsers(String userId);
}
