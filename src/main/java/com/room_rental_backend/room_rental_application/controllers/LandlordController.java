package com.room_rental_backend.room_rental_application.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/landlord")
public class LandlordController {

    @PostMapping
    public String postMethodName(@RequestBody String entity) {

        return entity;
    }

}
