package com.room_rental_backend.room_rental_application.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.room_rental_backend.room_rental_application.enums.RoomStatus;
import com.room_rental_backend.room_rental_application.enums.TannentsPreferred;
import com.room_rental_backend.room_rental_application.models.base_entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Room extends BaseEntity {

    @Column(name = "room_title", nullable = false, length = 100)
    private String roomTitle;

    @Column(name = "description", nullable = true, length = 500)
    private String description;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RoomStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_tenants")
    @Builder.Default
    private List<TannentsPreferred> preferredTenants = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Column(name = "rules")
    private List<RoomRules> rules;

    @Column(name = "image_urls", nullable = true)
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Column(name = "facilities", nullable = true)
    private List<RoomFacilities> facilities;

    @Column(name = "room_type", nullable = true, length = 50)
    private String roomType;

    @Column(name = "floor_number", nullable = true)
    private Integer floorNumber;

    @Column(name = "total_rooms", nullable = true)
    private Integer totalRooms;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

}
