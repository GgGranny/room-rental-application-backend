// package com.room_rental_backend.room_rental_application.models;

// import java.time.LocalDate;

// import
// com.room_rental_backend.room_rental_application.enums.RoomPaymentScope;
// import com.room_rental_backend.room_rental_application.enums.RoomStatus;
// import
// com.room_rental_backend.room_rental_application.models.base_entity.BaseEntity;

// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.EnumType;
// import jakarta.persistence.Enumerated;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.ManyToOne;
// import jakarta.persistence.Table;
// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;

// @Entity
// @Table(name = "room_facilities")
// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// public class RoomFacilities extends BaseEntity {

// @ManyToOne
// @JoinColumn(name = "room_id", nullable = false)
// private Room room;

// @Column(nullable = false)
// private String facilityName;

// @Column(name = "description")
// private String description;

// @Column(nullable = false)
// @Builder.Default
// private boolean isAvailable = true;

// @Enumerated(EnumType.STRING)
// @Column(name = "status")
// @Builder.Default
// private RoomStatus status = RoomStatus.AVAILABLE;

// @Column(nullable = false)
// @Builder.Default
// private boolean isIncludedInRent = true;

// @Enumerated(EnumType.STRING)
// private RoomPaymentScope scope;

// @Column(name = "extra_charge")
// @Builder.Default
// private boolean extraCharge = false;

// @Column(name = "installation_year")
// private Integer installationYear;

// @Column(name = "last_maintenance_date")
// private LocalDate lastMaintenanceDate;

// @Column(name = "next_maintenance_due")
// private LocalDate nextMaintenanceDue;

// @Column(name = "maintenance_notes")
// private String maintenanceNotes;
// }
