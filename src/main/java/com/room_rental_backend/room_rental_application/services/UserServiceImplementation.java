package com.room_rental_backend.room_rental_application.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.room_rental_backend.room_rental_application.interfaces.UserService;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<Users> getAllUsers() {
        List<Users> users = userRepository.findAll();
        return users;
    }

    @Override
    public void deleteUsers(String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteUsers'");
    }

}
