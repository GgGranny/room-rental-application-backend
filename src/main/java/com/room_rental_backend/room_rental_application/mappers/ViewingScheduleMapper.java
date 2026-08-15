package com.room_rental_backend.room_rental_application.mappers;

import org.springframework.stereotype.Component;

import com.room_rental_backend.room_rental_application.dtos.responseDtos.ViewingScheduleResponseDto;
import com.room_rental_backend.room_rental_application.models.Landlord;
import com.room_rental_backend.room_rental_application.models.Property;
import com.room_rental_backend.room_rental_application.models.Room;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.models.ViewingSchedule;

// New API: entity -> response mapping for viewing requests.
@Component
public class ViewingScheduleMapper {

    public ViewingScheduleResponseDto toResponse(ViewingSchedule schedule) {
        Room room = schedule.getRoom();
        Property property = room != null ? room.getProperty() : null;
        Users tenant = schedule.getTenant();
        Landlord landlord = schedule.getLandlord();
        Users owner = landlord != null ? landlord.getUser() : null;

        return ViewingScheduleResponseDto.builder()
                .scheduleId(schedule.getId())
                .status(schedule.getStatus())
                .scheduledAt(schedule.getScheduledAt())
                .note(schedule.getNote())
                .responseNote(schedule.getResponseNote())
                .createdAt(schedule.getCreatedAt())
                .roomId(room != null ? room.getId() : null)
                .roomTitle(room != null ? room.getRoomTitle() : null)
                .roomLocation(room != null ? room.getLocation() : null)
                .propertyId(property != null ? property.getId() : null)
                .propertyName(property != null ? property.getPropertyName() : null)
                .tenantId(tenant != null ? tenant.getId() : null)
                .tenantName(fullName(tenant))
                .tenantEmail(tenant != null ? tenant.getEmail() : null)
                .tenantPhone(tenant != null ? tenant.getPhoneNumber() : null)
                .landlordId(landlord != null ? landlord.getId() : null)
                .landlordName(fullName(owner))
                .landlordEmail(owner != null ? owner.getEmail() : null)
                .landlordPhone(owner != null ? owner.getPhoneNumber() : null)
                .build();
    }

    private String fullName(Users user) {
        if (user == null) {
            return null;
        }
        String first = user.getFname() == null ? "" : user.getFname();
        String last = user.getLname() == null ? "" : user.getLname();
        String name = (first + " " + last).trim();
        return name.isEmpty() ? null : name;
    }
}
