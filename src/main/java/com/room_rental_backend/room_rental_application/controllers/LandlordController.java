package com.room_rental_backend.room_rental_application.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.room_rental_backend.room_rental_application.responseHandler.ApiResponse;
import com.room_rental_backend.room_rental_application.services.CountryAndCityLocationService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/landlord")
@RequiredArgsConstructor
public class LandlordController {
    private final CountryAndCityLocationService countryAndCityLocationService;

    @PostMapping
    public String postMethodName(@RequestBody String entity) {
        // TODO: process POST request

        return entity;
    }

    @GetMapping("countries")
    public ApiResponse<Object> getMethodName() {
        return countryAndCityLocationService.getCountries();
    }

}
