package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import lombok.Builder;

@Builder
public record ImageDataResponse(
                Double fileSize,

                Long id,

                String url,

                String contentType,

                String imageFor) {

}
