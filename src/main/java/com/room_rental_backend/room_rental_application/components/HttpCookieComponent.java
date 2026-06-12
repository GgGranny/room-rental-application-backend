package com.room_rental_backend.room_rental_application.components;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HttpCookieComponent {

    public  Cookie createCookie(String cookieName, String value, int maxAge) {
        Cookie cookie = new Cookie(cookieName, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    public String getCookieValue(HttpServletRequest request, String cookieName) {
        if(request.getCookies() == null) {
            return null;
        }
        if(request.getCookies() != null) {
            for(Cookie cookie: request.getCookies()) {
                if("refreshToken".equalsIgnoreCase(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
