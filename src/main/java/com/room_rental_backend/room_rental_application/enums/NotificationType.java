package com.room_rental_backend.room_rental_application.enums;

// New API: the business event a push notification represents. Each type also
// carries the "action" the frontend should perform (i.e. which page to open)
// when the user taps the notification.
public enum NotificationType {
    VIEWING_REQUEST("OPEN_VIEWING"),
    VIEWING_ACCEPTED("OPEN_VIEWING"),
    VIEWING_REJECTED("OPEN_VIEWING"),
    VIEWING_CANCELLED("OPEN_VIEWING"),
    PROPERTY_APPROVED("OPEN_PROPERTY"),
    PROPERTY_REJECTED("OPEN_PROPERTY"),
    KYC_APPROVED("OPEN_KYC"),
    KYC_REJECTED("OPEN_KYC"),
    PAYMENT_SUCCESS("OPEN_PAYMENT"),
    PAYMENT_FAILED("OPEN_PAYMENT");

    private final String action;

    NotificationType(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}