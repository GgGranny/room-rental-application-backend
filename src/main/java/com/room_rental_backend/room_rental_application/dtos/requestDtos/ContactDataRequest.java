package com.room_rental_backend.room_rental_application.dtos.requestDtos;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactDataRequest {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Phone must be in E.164 format e.g. +9779XXXXXXXX")
    private String phoneNumber;

    @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Alternate phone must be in E.164 format")
    @Nullable
    private String alternatePhone; // optional
}
