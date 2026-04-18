package com.room_rental_backend.room_rental_application.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/admin/")
public class AdminController {

    @GetMapping
    public String getMethodName() {
        return "this is admin";
    }

}
