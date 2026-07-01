// package com.room_rental_backend.room_rental_application.models;

// import java.time.LocalTime;

// import
// com.room_rental_backend.room_rental_application.models.base_entity.BaseEntity;

// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.ManyToOne;
// import jakarta.persistence.Table;
// import lombok.AllArgsConstructor;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;

// // @Entity
// // @Table(name = "room_rules")
// // @Getter
// // @Setter
// // @AllArgsConstructor
// // @NoArgsConstructor
// public class RoomRules extends BaseEntity {

// @ManyToOne
// @JoinColumn(name = "room_id")
// private Room room;

// @Column(nullable = false)
// private Integer maxOccupants = 1;

// @Column(nullable = false)
// private boolean smokingAllowed = false;

// @Column(nullable = false)
// private boolean petsAllowed = false;

// @Column(nullable = false)
// private boolean visitorsAllowed = true;

// @Column(nullable = false)
// private boolean overnightGuestsAllowed = false;

// @Column(nullable = false)
// private boolean partiesAllowed = false;

// @Column(name = "quiet_hours_start", nullable = true)
// private LocalTime quietHoursStart;

// @Column(name = "quiet_hours_end", nullable = true)
// private LocalTime quietHoursEnd;

// @Column(nullable = true)
// private boolean loudMusicAllowed = false;

// @Column(name = "guest_check_in_cutoff", nullable = true)
// private LocalTime guestCheckInCutoff;

// @Column(columnDefinition = "TEXT")
// private String additionalRules;

// @Column(nullable = false)
// private boolean isActive = true;

// @Column(nullable = false)
// private boolean tenantMustAcknowledge = true;

// }
