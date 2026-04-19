package com.room_rental_backend.room_rental_application.components;

import java.util.Map;

import com.room_rental_backend.room_rental_application.models.GoogleOAuth2UserInfo;
import com.room_rental_backend.room_rental_application.models.base_entity.OAuth2UserInfo;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(
            String provider,
            Map<String, Object> attributes) {

        return switch (provider.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            default -> throw new RuntimeException("Provider not supported: " + provider);
        };
    }
}