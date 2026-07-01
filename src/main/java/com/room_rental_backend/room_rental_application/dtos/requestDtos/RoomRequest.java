package com.room_rental_backend.room_rental_application.dtos.requestDtos;

import java.math.BigDecimal;
import java.util.List;

import com.room_rental_backend.room_rental_application.enums.RoomStatus;
import com.room_rental_backend.room_rental_application.enums.TannentsPreferred;

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
public class RoomRequest {

    @NotBlank
    private String roomTitle;

    private String description;

    @NotBlank
    private String location;

    @NotNull
    private BigDecimal price;

    private RoomStatus status;

    private List<TannentsPreferred> preferredTenants;

    private List<String> rules;

    private List<String> facilities;

    private String roomType;

    private Integer floorNumber;

    private Integer totalRooms;

    @NotBlank
    private String propertyId;

    private String address;

    private Double latitude;

    private Double longitude;

}
