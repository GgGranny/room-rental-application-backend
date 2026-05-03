package com.room_rental_backend.room_rental_application.dtos.requestDtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record RegisterUserRequestDtos(

        @NotNull(message = "email is required") @Email(message = "invalid email format") String email,

        @NotNull(message = "password is required") String password

// @Size(min = 10, max = 10, message = "phone number shound be of 10 digits")
// @NotNull(message = "phone number is required") String phoneNumber,

// @NotBlank(message = "fname is required") String fname,

// @NotBlank(message = "lname is required") String lname,

// @NotNull(message = "role is not provided") Roles role,

// @NotNull(message = "dob is required") String dob
) {

}
