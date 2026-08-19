package com.room_rental_backend.room_rental_application.services;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.ViewingScheduleRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.ViewingScheduleResponseDto;
import com.room_rental_backend.room_rental_application.enums.ScheduleStatus;
import com.room_rental_backend.room_rental_application.exceptions.UnauthorizedException;
import com.room_rental_backend.room_rental_application.exceptions.UserNotFoundException;
import com.room_rental_backend.room_rental_application.interfaces.ViewingScheduleService;
import com.room_rental_backend.room_rental_application.mappers.ViewingScheduleMapper;
import com.room_rental_backend.room_rental_application.models.Landlord;
import com.room_rental_backend.room_rental_application.models.Property;
import com.room_rental_backend.room_rental_application.models.Room;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.models.ViewingSchedule;
import com.room_rental_backend.room_rental_application.repositories.RoomRepository;
import com.room_rental_backend.room_rental_application.repositories.UserRepository;
import com.room_rental_backend.room_rental_application.repositories.ViewingScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// New API: implements the tenant<->landlord viewing-request workflow. The
// authenticated principal is resolved from the JWT (email) on every call, and
// ownership is enforced server-side so tenants and landlords can only touch
// their own records.
@Service
@RequiredArgsConstructor
@Slf4j
public class ViewingScheduleServiceImplementation implements ViewingScheduleService {

    private final ViewingScheduleRepository scheduleRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ViewingScheduleMapper scheduleMapper;

    @Transactional
    @Override
    public ViewingScheduleResponseDto createSchedule(ViewingScheduleRequest request, Authentication authentication) {
        Users tenant = resolveUser(authentication);

        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found for id: " + request.roomId()));

        Property property = room.getProperty();
        if (property == null || property.getLandlord() == null) {
            throw new IllegalArgumentException("This room is not available for viewing requests");
        }
        Landlord landlord = property.getLandlord();

        // A landlord should not be able to request a viewing on their own room.
        if (landlord.getUser() != null && landlord.getUser().getId().equals(tenant.getId())) {
            throw new UnauthorizedException("You cannot request a viewing for your own room");
        }

        // Prevent duplicate pending or approved requests by the same tenant for the same slot.
        boolean duplicateExists = scheduleRepository.existsByRoomAndTenantAndScheduledAtAndStatusIn(
                room, tenant, request.scheduledAt(), List.of(ScheduleStatus.PENDING, ScheduleStatus.APPROVED));
        if (duplicateExists) {
            throw new IllegalArgumentException("You already have a pending or approved viewing request for this room at the chosen time");
        }

        ViewingSchedule schedule = ViewingSchedule.builder()
                .room(room)
                .tenant(tenant)
                .landlord(landlord)
                .scheduledAt(request.scheduledAt())
                .note(request.note())
                .status(ScheduleStatus.PENDING)
                .build();

        return scheduleMapper.toResponse(scheduleRepository.save(schedule));
    }

    @Override
    public List<ViewingScheduleResponseDto> getMySchedulesAsTenant(Authentication authentication) {
        Users tenant = resolveUser(authentication);
        return scheduleRepository.findByTenantOrderByScheduledAtDesc(tenant)
                .stream()
                .map(scheduleMapper::toResponse)
                .toList();
    }

    @Override
    public List<ViewingScheduleResponseDto> getMySchedulesAsLandlord(Authentication authentication) {
        Users owner = resolveUser(authentication);
        Landlord landlord = owner.getLandlord();
        if (landlord == null) {
            throw new UnauthorizedException("Only landlords can view viewing requests");
        }
        return scheduleRepository.findByLandlordOrderByScheduledAtDesc(landlord)
                .stream()
                .map(scheduleMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public ViewingScheduleResponseDto respondToSchedule(String scheduleId, String status, String responseNote,
            Authentication authentication) {
        Users owner = resolveUser(authentication);
        ViewingSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found for id: " + scheduleId));

        // Only the owning landlord may respond.
        if (schedule.getLandlord() == null || schedule.getLandlord().getUser() == null
                || !schedule.getLandlord().getUser().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You can only respond to viewing requests for your own properties");
        }

        ScheduleStatus target = parseStatus(status);
        if (target != ScheduleStatus.APPROVED && target != ScheduleStatus.REJECTED) {
            throw new IllegalArgumentException("A landlord can only APPROVE or REJECT a request");
        }
        if (schedule.getStatus() != ScheduleStatus.PENDING) {
            throw new IllegalArgumentException("This request has already been " + schedule.getStatus());
        }

        schedule.setStatus(target);
        schedule.setResponseNote(responseNote);
        ViewingSchedule saved = scheduleRepository.save(schedule);

        // When a slot is APPROVED, automatically reject all other PENDING requests for the exact same room and slot.
        if (target == ScheduleStatus.APPROVED) {
            List<ViewingSchedule> conflicts = scheduleRepository.findByRoomAndScheduledAtAndStatusAndIdNot(
                    schedule.getRoom(), schedule.getScheduledAt(), ScheduleStatus.PENDING, schedule.getId());
            for (ViewingSchedule conflict : conflicts) {
                conflict.setStatus(ScheduleStatus.REJECTED);
                conflict.setResponseNote("Slot automatically rejected due to another approved viewing request at this time");
                scheduleRepository.save(conflict);
            }
        }

        return scheduleMapper.toResponse(saved);
    }

    @Transactional
    @Override
    public ViewingScheduleResponseDto cancelSchedule(String scheduleId, Authentication authentication) {
        Users tenant = resolveUser(authentication);
        ViewingSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found for id: " + scheduleId));

        // Only the tenant who created the request may cancel it.
        if (schedule.getTenant() == null || !schedule.getTenant().getId().equals(tenant.getId())) {
            throw new UnauthorizedException("You can only cancel your own viewing requests");
        }
        if (schedule.getStatus() != ScheduleStatus.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be cancelled");
        }

        schedule.setStatus(ScheduleStatus.CANCELLED);
        return scheduleMapper.toResponse(scheduleRepository.save(schedule));
    }

    // Resolve the authenticated principal to a persisted user, or fail.
    private Users resolveUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));
    }

    private ScheduleStatus parseStatus(String status) {
        try {
            return ScheduleStatus.valueOf(status.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid schedule status: " + status);
        }
    }
}
