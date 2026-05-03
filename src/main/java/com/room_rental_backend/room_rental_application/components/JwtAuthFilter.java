package com.room_rental_backend.room_rental_application.components;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.room_rental_backend.room_rental_application.services.CustomUserDetailsService;
import com.room_rental_backend.room_rental_application.services.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final CustomUserDetailsService customUserDetailsService;
    // private static final List<String> PERMITTED_PATHS = List.of(
    // "/api/v1/auth/",
    // "/api/v1/rooms/",
    // "/oauth2/",
    // "/login/oauth2/",
    // "/h2-console/");

    // @Override
    // protected boolean shouldNotFilter(HttpServletRequest request) {
    // String path = request.getRequestURI();
    // boolean skip = PERMITTED_PATHS.stream().anyMatch(path::startsWith);
    // if (skip)
    // log.debug("Skipping JWT filter for: {}", path);
    // return skip;
    // }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // extracting the Authorization header
        String authHeader = request.getHeader("Authorization");

        // Check if the header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtService.extractUsername(token);
            /*
             * If email is present and the user is not authenticated yet
             * And Set the authentication in the SecurityContext
             */
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var userDetails = customUserDetailsService.loadUserByUsername(email);

                if (jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Token expired for: {}", request.getRequestURI());

        } catch (io.jsonwebtoken.JwtException e) {
            log.warn("Invalid JWT: {}", e.getMessage());

        } catch (Exception e) {
            log.error("JWT filter error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

}
