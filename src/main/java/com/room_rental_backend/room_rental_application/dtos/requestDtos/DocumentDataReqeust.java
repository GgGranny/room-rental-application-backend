package com.room_rental_backend.room_rental_application.dtos.requestDtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDataReqeust {

    @NotNull(message = "Document type is required")
    @Pattern(regexp = "CITIZENSHIP|PASSPORT|LICENSE|LISCENSE|NATIONAL_ID|DRIVERS_LICENSE", message = "Invalid document type")
    private String documentType;

    private String frontImageUrl;

    private String backImageUrl;

    private String selfieUrl;

}
