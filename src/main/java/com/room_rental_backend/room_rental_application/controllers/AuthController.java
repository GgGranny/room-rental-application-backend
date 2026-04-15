package com.room_rental_backend.room_rental_application.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.UserLoginRequestDto;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @PostMapping("/login")
    public String postMethodName(@RequestBody UserLoginRequestDto entity) {
        System.out.println("hello there");
        return "hello world";
    }

}
