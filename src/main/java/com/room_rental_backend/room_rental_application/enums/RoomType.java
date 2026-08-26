package com.room_rental_backend.room_rental_application.enums;

// Room type / category. PRIVATE and SHARED additionally drive occupancy: SHARED
// rooms unlock the Roommate Finder, everything else follows the normal rental
// flow only. APARTMENT..OTHER were consolidated here from the former (unused)
// RomTypes enum so there is a single source of truth for room types.
public enum RoomType {
    PRIVATE,
    SHARED,
    APARTMENT,
    HOUSE,
    HOSTEL,
    GARAGE,
    COMMERCIAL,
    OTHER
}
