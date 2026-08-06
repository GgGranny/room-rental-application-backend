package com.room_rental_backend.room_rental_application.controllers;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

import com.room_rental_backend.room_rental_application.components.HttpCookieComponent;
import com.room_rental_backend.room_rental_application.exceptions.TokenNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.oauth2.sdk.Response;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.CompleteUserProfileRequest;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.RefreshTokenRequest;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.RegisterUserRequestDtos;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.UserLoginRequestDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.AuthResponse;
import com.room_rental_backend.room_rental_application.interfaces.AuthService;
import com.room_rental_backend.room_rental_application.responseHandler.ApiResponse;
import com.room_rental_backend.room_rental_application.responseHandler.GlobalResponseHandler;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth/")
@RequiredArgsConstructor
public class AuthController {

        private final AuthService authService;

        private final HttpCookieComponent httpCookieComponent;

        @Value("${access.token.expiration}")
        private int accessTokenAge;

        @Value("${refresh.token.expiration}")
        private int refreshTokenAge;

        @PostMapping("login")
        public ResponseEntity<ApiResponse<AuthResponse>> login(
                        @Valid @RequestBody UserLoginRequestDto entity,
                        HttpServletResponse rs) {
                AuthResponse response = authService.login(entity);

                Cookie accessToken = httpCookieComponent.createCookie("accessToken", response.token(), accessTokenAge);
                Cookie refreshToken = httpCookieComponent.createCookie("refreshToken", response.refreshToken(),
                                refreshTokenAge);
                rs.addCookie(accessToken);
                rs.addCookie(refreshToken);
                // A verified user can log in before completing role selection; do not fail that
                // flow.
                if (response.role() != null) {
                        Cookie roleCookie = httpCookieComponent.createCookie("role", response.role().toString(),
                                        accessTokenAge);
                        rs.addCookie(roleCookie);
                }
                return GlobalResponseHandler.success(
                                "Login Successful",
                                response,
                                HttpStatus.OK);
        }

        @PostMapping("register")
        public ResponseEntity<ApiResponse<Object>> register(
                        @Valid @RequestBody RegisterUserRequestDtos registerUserRequestDtos) {
                authService.register(registerUserRequestDtos);
                return GlobalResponseHandler.success(
                                "Please Verify your account",
                                null,
                                HttpStatus.CREATED);
        }

        @GetMapping("activate")
        public ResponseEntity<ApiResponse<Object>> activateUserAccount(@RequestParam("token") String otp) {
                authService.activateUser(otp);
                return GlobalResponseHandler.success(
                                "Verification Successful, Please Login",
                                null,
                                HttpStatus.OK);
        }

        // @PostMapping("refresh")
        // public ResponseEntity<ApiResponse<AuthResponse>> refreshtokenValidation(
        // @RequestBody RefreshTokenRequest refreshToken) {
        // AuthResponse response = authService.refreshToken(refreshToken);
        // return GlobalResponseHandler.success(
        // "Login Successful",
        // response,
        // HttpStatus.OK);
        // }

        @PostMapping("refresh")
        public ResponseEntity<ApiResponse<AuthResponse>> refreshtokenValidation(
                        HttpServletRequest request,
                        HttpServletResponse rs) {
                String refreshToken = httpCookieComponent.getCookieValue(request, "refreshToken");
                if (refreshToken == null) {
                        throw new TokenNotFoundException("Refresh token is empty");
                }

                AuthResponse response = authService.refreshToken(refreshToken);

                Cookie newAccessTokenCookie = httpCookieComponent.createCookie("accessToken", response.token(),
                                accessTokenAge);
                rs.addCookie(newAccessTokenCookie);
                return GlobalResponseHandler.success(
                                "New Token Created Successfully",
                                response,
                                HttpStatus.OK);
        }

        @PostMapping("complete-profile")
        public ResponseEntity<ApiResponse<AuthResponse>> completeUserProfile(
                        @RequestBody CompleteUserProfileRequest request,
                        HttpServletResponse rs) {
                AuthResponse response = authService.completeUserProfile(request);
                Cookie accessToken = httpCookieComponent.createCookie("accessToken", response.token(), accessTokenAge);
                Cookie refreshToken = httpCookieComponent.createCookie("refreshToken", response.refreshToken(),
                                refreshTokenAge);
                rs.addCookie(accessToken);
                rs.addCookie(refreshToken);
                return GlobalResponseHandler.success(
                                "Setup successful",
                                response,
                                HttpStatus.OK);
        }

        @GetMapping("is-profile-complete")
        public ResponseEntity<ApiResponse<Object>> checkUserProfileCompletion() {
                boolean isCompleted = authService.isProfileCompleted();
                Map<String, Boolean> response = new HashMap<>();
                if (isCompleted) {
                        response.put("isCompleted", true);
                        return GlobalResponseHandler.success(
                                        "Profile setup is completed",
                                        response,
                                        HttpStatus.OK);
                }
                response.put("isCompleted", false);
                return GlobalResponseHandler.error(
                                "Profile setup is not completed",
                                response,
                                HttpStatus.OK);
        }

        @GetMapping("/me")
        public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser(Authentication authentication)
                        throws Exception {
                AuthResponse response = authService.getCurrentUser(authentication);
                return GlobalResponseHandler.success(
                                "User found successfully",
                                response,
                                HttpStatus.OK);
        }

}
