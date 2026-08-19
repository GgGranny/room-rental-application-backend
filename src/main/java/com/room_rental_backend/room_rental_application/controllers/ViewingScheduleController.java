package com.room_rental_backend.room_rental_application.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.ViewingScheduleDecisionRequest;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.ViewingScheduleRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.ViewingScheduleResponseDto;
import com.room_rental_backend.room_rental_application.interfaces.ViewingScheduleService;
import com.room_rental_backend.room_rental_application.responseHandler.ApiResponse;
import com.room_rental_backend.room_rental_application.responseHandler.GlobalResponseHandler;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// New API: room-viewing scheduling endpoints.
// Path-based security (see SpringSecurity): /api/v1/schedules/landlord/** is
// LANDLORD-only, the rest of /api/v1/schedules/** is USER-only. The service also
// re-checks ownership on every mutation.
@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ViewingScheduleController {

    private final ViewingScheduleService viewingScheduleService;

    // Tenant: request a viewing.
    @PostMapping
    public ResponseEntity<ApiResponse<ViewingScheduleResponseDto>> createSchedule(
            @Valid @RequestBody ViewingScheduleRequest request,
            Authentication authentication) {
        ViewingScheduleResponseDto response = viewingScheduleService.createSchedule(request, authentication);
        return GlobalResponseHandler.success("Viewing request submitted successfully", response, HttpStatus.CREATED);
    }

    // Tenant: list my viewing requests.
    @GetMapping("/tenant")
    public ResponseEntity<ApiResponse<List<ViewingScheduleResponseDto>>> getMySchedules(
            Authentication authentication) {
        List<ViewingScheduleResponseDto> response = viewingScheduleService.getMySchedulesAsTenant(authentication);
        return GlobalResponseHandler.success("Viewing requests fetched successfully", response, HttpStatus.OK);
    }

    // Tenant: cancel a pending request.
    @PatchMapping("/{scheduleId}/cancel")
    public ResponseEntity<ApiResponse<ViewingScheduleResponseDto>> cancelSchedule(
            @PathVariable("scheduleId") String scheduleId,
            Authentication authentication) {
        ViewingScheduleResponseDto response = viewingScheduleService.cancelSchedule(scheduleId, authentication);
        return GlobalResponseHandler.success("Viewing request cancelled successfully", response, HttpStatus.OK);
    }

    // Landlord: list viewing requests for my properties.
    @GetMapping("/landlord")
    public ResponseEntity<ApiResponse<List<ViewingScheduleResponseDto>>> getLandlordSchedules(
            Authentication authentication) {
        List<ViewingScheduleResponseDto> response = viewingScheduleService.getMySchedulesAsLandlord(authentication);
        return GlobalResponseHandler.success("Viewing requests fetched successfully", response, HttpStatus.OK);
    }

    // Landlord: approve or reject a viewing request.
    @PatchMapping("/landlord/{scheduleId}/respond")
    public ResponseEntity<ApiResponse<ViewingScheduleResponseDto>> respondToSchedule(
            @PathVariable("scheduleId") String scheduleId,
            @Valid @RequestBody ViewingScheduleDecisionRequest request,
            Authentication authentication) {
        ViewingScheduleResponseDto response = viewingScheduleService.respondToSchedule(
                scheduleId, request.status(), request.responseNote(), authentication);
        return GlobalResponseHandler.success("Viewing request updated successfully", response, HttpStatus.OK);
    }
}
