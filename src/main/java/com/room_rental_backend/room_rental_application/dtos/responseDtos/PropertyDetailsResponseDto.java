package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PropertyDetailsResponseDto {

    private String id;
    private String propertyName;
    private String propertyStatus;
    private String description;
    private String thumbnailUrl;

    private String address;
    private String city;
    private String district;
    private String province;
    private String zipCode;
    private String country;

    private UserResponse landlord;

    private List<RoomResponseDto> rooms;
}