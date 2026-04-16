package com.room_rental_backend.room_rental_application.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.RegisterUserRequestDtos;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.UserLoginRequestDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.AuthResponse;
import com.room_rental_backend.room_rental_application.exceptions.EmailAlreadyExistsException;
import com.room_rental_backend.room_rental_application.exceptions.PhoneNumberAlreadyExists;
import com.room_rental_backend.room_rental_application.exceptions.UserNotFoundException;
import com.room_rental_backend.room_rental_application.interfaces.AuthService;
import com.room_rental_backend.room_rental_application.mappers.AuthMapper;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImplementation implements AuthService {

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final AuthMapper authMapper;

    private final JwtService jwtService;

    @Override
    public AuthResponse register(RegisterUserRequestDtos request) {

        String email = request.email().trim().toLowerCase();
        String phoneNumber = request.phoneNumber().trim();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new PhoneNumberAlreadyExists("Phone number already exists");
        }
        Users newUser = authMapper.toUsers(request);
        Users savedUser = userRepository.save(newUser);

        String jwtToken = jwtService.generateToken(savedUser);
        return authMapper.toAuthResponse(savedUser, jwtToken);
    }

    @Override
    public AuthResponse login(UserLoginRequestDto request) {
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        Users user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("No User For For " + request.email() + "Email"));
        String jwtToken = jwtService.generateToken(user);
        return authMapper.toAuthResponse(user, jwtToken);
    }

}
