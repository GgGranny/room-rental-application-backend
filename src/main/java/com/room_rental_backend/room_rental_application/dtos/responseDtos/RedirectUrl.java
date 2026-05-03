package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import org.springframework.http.HttpStatus;

import lombok.Builder;

@Builder
public record RedirectUrl(
                HttpStatus code,
                String pageName,
                String url) {
}
