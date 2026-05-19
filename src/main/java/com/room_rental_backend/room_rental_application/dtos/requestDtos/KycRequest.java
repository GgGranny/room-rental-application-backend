
package com.room_rental_backend.room_rental_application.dtos.requestDtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycRequest {

    @NotBlank(message = "user id required")
    private String customerId;

    @Valid
    @NotNull(message = "Fill in the personal info")
    private PersonalDataRequest personalInfo;

    @Valid
    @NotNull(message = "Fill in the document info")
    private DocumentDataReqeust document;

    @Valid
    @NotNull(message = "Complete the address info")
    private AddressDataRequest address;

    @Valid
    @NotNull(message = "Fill in the contact info")
    private ContactDataRequest contact;
}