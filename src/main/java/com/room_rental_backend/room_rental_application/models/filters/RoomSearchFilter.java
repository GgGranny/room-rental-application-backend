package com.room_rental_backend.room_rental_application.models.filters;

import java.math.BigDecimal;
import java.util.List;

import com.room_rental_backend.room_rental_application.enums.TannentsPreferred;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomSearchFilter {

    private String location;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private String roomType;

    private List<TannentsPreferred> preferredTenants;
}
