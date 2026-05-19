package com.room_rental_backend.room_rental_application.dtos.requestDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDataRequest {

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 100, message = "Address line 1 must not exceed 100 characters")
    private String addressLine1;

    @Size(max = 100, message = "Address line 2 must not exceed 100 characters")
    private String addressLine2; // optional

    @NotBlank(message = "City is required")
    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;

    @Size(max = 50)
    private String state; // optional

    @NotBlank(message = "Postal code is required")
    @Size(max = 10, message = "Postal code must not exceed 10 characters")
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 3, message = "Use ISO country code e.g. NP, US")
    private String country;
}
