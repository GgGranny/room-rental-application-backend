package com.room_rental_backend.room_rental_application.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.room_rental_backend.room_rental_application.enums.DocumentTypes;
import com.room_rental_backend.room_rental_application.enums.KycStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "kyc")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Kyc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "middle_name")
    private String middleName; // optional

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private String gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentTypes documentType;

    @Builder.Default
    @Column(name = "selfie_url")
    private String selfieUrl = null;

    @Column(name = "front_document_url", nullable = false)
    private String frontImageUrl;

    @Builder.Default
    @Column(name = "back_document_url")
    private String backImageUrl = null;

    @Column(name = "address1", nullable = false)
    private String addressLine1;

    @Column(name = "address2")
    private String addressLine2; // optional

    @Column(nullable = false)
    private String city;

    private String state; // optional

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String country;

    @Enumerated(EnumType.STRING)
    private KycStatus status;

    @Builder.Default
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "phone_number", nullable = false)
    @Size(max = 16)
    private String phoneNumber;

    @Column(name = "alt_phone_number")
    @Size(max = 16)
    private String altPhoneNumber;
}
