package com.room_rental_backend.room_rental_application.dtos.requestDtos;

import com.room_rental_backend.room_rental_application.enums.PropertyStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record PropertyRequest(
                @NotBlank String propertyName,

                @NotNull String landlordId,

                @NotNull PropertyStatus propertyStatus,

                @NotBlank String description,

                // @NotBlank String address,

                @NotBlank String city,

                @NotBlank String district,

                @NotBlank String province,

                @NotBlank String zipCode,

                @NotBlank String country) {

}
