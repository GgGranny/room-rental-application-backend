package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import com.room_rental_backend.room_rental_application.enums.DocumentTypes;
import com.room_rental_backend.room_rental_application.enums.KycStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycResponse {
    private int kycId;
    private String customerId;
    private KycStatus kycStatus;
    private LocalDateTime submittedAt;

    private String firstName;
    private String lastName;
    private String middleName;
    private LocalDate dateOfBirth;
    private String gender;

    private DocumentTypes documentType;
    private String frontImageUrl;
    private String backImageUrl;
    private String selfieUrl;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    private String phoneNumber;
    private String alternatePhone;
}
