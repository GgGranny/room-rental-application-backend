package com.room_rental_backend.room_rental_application.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.room_rental_backend.room_rental_application.enums.RoomStatus;
import com.room_rental_backend.room_rental_application.enums.RoomType;
import com.room_rental_backend.room_rental_application.enums.TannentsPreferred;
import com.room_rental_backend.room_rental_application.models.base_entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "room_preferred_tenants", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "preferred_tenant")
    @Builder.Default
    private List<TannentsPreferred> preferredTenants = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "room_rule_texts", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "rule", nullable = true)
    @Builder.Default
    private List<String> rules = new ArrayList<>();

    // @ElementCollection(fetch = FetchType.LAZY)
    // @CollectionTable(name = "room_image_urls", joinColumns = @JoinColumn(name =
    // "room_id"))
    // @Column(name = "image_urls", nullable = true)
    // @Builder.Default
    // private List<String> imageUrls = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ImageMetadata> images = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "room_facility_names", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "facility", nullable = true)
    @Builder.Default
    private List<String> facilities = new ArrayList<>();

    @Column(name = "room_type", nullable = true, length = 50)
    private String roomType;

    // New API: strict PRIVATE/SHARED occupancy type (distinct from the free-text
    // room_type category). SHARED rooms enable the Roommate Finder.
    @Enumerated(EnumType.STRING)
    @Column(name = "sharing_type", nullable = true, length = 20)
    private RoomType sharingType;

    @Column(name = "floor_number", nullable = true)
    private Integer floorNumber;

    @Column(name = "total_rooms", nullable = true)
    private Integer totalRooms;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "address")
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

}
