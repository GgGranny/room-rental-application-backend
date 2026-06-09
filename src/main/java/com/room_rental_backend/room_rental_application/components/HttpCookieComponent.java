package com.room_rental_backend.room_rental_application.components;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HttpCookieComponent {

    public  Cookie createCookie(String cookieName, String value) {
        Cookie cookie = new Cookie(cookieName, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }
}
