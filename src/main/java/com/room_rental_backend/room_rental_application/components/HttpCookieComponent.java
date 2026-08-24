package com.room_rental_backend.room_rental_application.components;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HttpCookieComponent {

    @Value("${app.cookie.secure:false}")
    private boolean secureCookies;

    public Cookie createCookie(String cookieName, String value, int maxAge) {
        Cookie cookie = new Cookie(cookieName, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    // Adds the actual browser cookie with explicit security attributes. JWTs stay
    // HttpOnly and never need to be copied into localStorage by the frontend.
    public void addCookie(HttpServletResponse response, String cookieName, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, value == null ? "" : value)
                .httpOnly(true).secure(secureCookies).sameSite("Lax").path("/").maxAge(maxAge).build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public String getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (cookieName != null && cookieName.equalsIgnoreCase(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
