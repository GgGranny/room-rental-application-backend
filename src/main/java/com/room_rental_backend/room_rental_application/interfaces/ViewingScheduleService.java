package com.room_rental_backend.room_rental_application.interfaces;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.ViewingScheduleRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.ViewingScheduleResponseDto;

// New API: room-viewing scheduling. Tenants request viewings; landlords decide.
public interface ViewingScheduleService {

    // Tenant creates a viewing request for a room.
    ViewingScheduleResponseDto createSchedule(ViewingScheduleRequest request, Authentication authentication);

    // Tenant lists their own viewing requests.
    List<ViewingScheduleResponseDto> getMySchedulesAsTenant(Authentication authentication);

    // Landlord lists viewing requests for their properties.
    List<ViewingScheduleResponseDto> getMySchedulesAsLandlord(Authentication authentication);

    // Landlord approves or rejects a request they own (APPROVED / REJECTED).
    ViewingScheduleResponseDto respondToSchedule(String scheduleId, String status, String responseNote,
            Authentication authentication);

    // Tenant cancels their own pending request.
    ViewingScheduleResponseDto cancelSchedule(String scheduleId, Authentication authentication);
}
