package com.room_rental_backend.room_rental_application.models;

import java.time.LocalDateTime;

import com.room_rental_backend.room_rental_application.enums.ScheduleStatus;
import com.room_rental_backend.room_rental_application.models.base_entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// New API: a tenant's request to visit/tour a room at a proposed time. The
// landlord reviews the requested slot and approves or rejects it. This replaces
// the old "booking" concept — there is no room reservation, only a viewing.
@Entity
@Table(name = "viewing_schedules")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ViewingSchedule extends BaseEntity {

    // The room the tenant wants to view.
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // The tenant who requested the viewing.
    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Users tenant;

    // Denormalised owner reference so the landlord can query their own requests
    // without walking room -> property -> landlord every time.
    @ManyToOne
    @JoinColumn(name = "landlord_id", nullable = false)
    private Landlord landlord;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "note", length = 500)
    private String note;

    // Landlord's reason when rejecting (optional).
    @Column(name = "response_note", length = 500)
    private String responseNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ScheduleStatus status = ScheduleStatus.PENDING;
}
