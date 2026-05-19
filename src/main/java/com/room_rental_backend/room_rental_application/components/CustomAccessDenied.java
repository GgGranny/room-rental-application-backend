package com.room_rental_backend.room_rental_application.components;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class CustomAccessDenied implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        Map<String, Object> body = Map.of(
                "status", 403,
                "error", "Forbidden",
                "message", "You don't have permission to access this resource",
                "path", request.getServletPath());

        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }

}
