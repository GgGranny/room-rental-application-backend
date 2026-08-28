package com.room_rental_backend.room_rental_application.enums;

// New API: lifecycle of a confirmed roommate match. ACTIVE matches allow the two
// tenants to chat; ENDED keeps the history readable but blocks new messages.
public enum MatchStatus {
    ACTIVE,
    ENDED
}
