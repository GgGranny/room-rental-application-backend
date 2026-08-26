package com.room_rental_backend.room_rental_application.dtos.responseDtos;

import java.math.BigDecimal;
import java.util.List;

import com.room_rental_backend.room_rental_application.enums.RoomStatus;
import com.room_rental_backend.room_rental_application.enums.RoomType;
import com.room_rental_backend.room_rental_application.enums.TannentsPreferred;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomDetailsResponseDto {
    private String roomId;

    private String roomTitle;
    private String description;
    private String location;

    private BigDecimal price;
    private RoomStatus status;

    private List<TannentsPreferred> preferredTenants;

    private String roomType;
    private RoomType sharingType;
    private Integer floorNumber;
    private Integer totalRooms;

    private List<ImageDataResponse> imageUrls;

    private List<String> facilities;
    private List<String> rules;

    private String propertyId;
    private String propertyName;

    private String city;
    private String district;
    private String province;

    private String address;

    private Double latitude;

    private Double Longitude;

    private UserResponse userResponse;
}
