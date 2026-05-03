package com.room_rental_backend.room_rental_application.components;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.room_rental_backend.room_rental_application.dtos.responseDtos.AuthResponse;
import com.room_rental_backend.room_rental_application.exceptions.EmailNotFoundException;
import com.room_rental_backend.room_rental_application.exceptions.UserNotFoundException;
import com.room_rental_backend.room_rental_application.interfaces.RefreshTokenService;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.repositories.UserRepository;
import com.room_rental_backend.room_rental_application.responseHandler.ApiResponse;
import com.room_rental_backend.room_rental_application.services.JwtService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

        private final JwtService jwtService;
        private final RefreshTokenService refreshTokenService;
        private final UserRepository userRepository;
        private final ObjectMapper objectMapper;

        @Value("${oauth2.login.success.url}")
        private String redirectUrl;

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                        Authentication authentication) throws IOException, ServletException {

                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                String email = oAuth2User.getAttribute("email");

                Users user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UserNotFoundException("User for " + email + " Does not exists"));

                String jwtToken = jwtService.generateToken(user);
                String refreshToken = refreshTokenService.generateRefreshToken();

                // String targetUrl = UriComponentsBuilder
                // .fromUriString(redirectUrl)
                // .queryParam("accessToken", jwtToken)
                // .queryParam("refreshToken", refreshToken)
                // .build().toUriString();
                // System.out.println("success google login: " + targetUrl);
                // getRedirectStrategy().sendRedirect(request, response, targetUrl);

                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_OK);
                response.setCharacterEncoding("UTF-8");
                Map<String, Object> responseBuilder = new HashMap<>();
                responseBuilder.put("token", jwtToken);
                responseBuilder.put("refreshToken", refreshToken);
                responseBuilder.put("userId", user.getId());
                responseBuilder.put("isVerified", user.isVerified());

                ApiResponse<Map<String, Object>> res = new ApiResponse<>();
                res.setData(responseBuilder);
                res.setMessage("Login successful");
                res.setSuccess(true);

                response.getWriter().write(objectMapper.writeValueAsString(res));
        }
}
