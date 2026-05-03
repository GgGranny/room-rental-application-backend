package com.room_rental_backend.room_rental_application.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.connector.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.room_rental_backend.room_rental_application.dtos.responseDtos.AuthResponse;
import com.room_rental_backend.room_rental_application.responseHandler.ApiResponse;
import com.room_rental_backend.room_rental_application.responseHandler.GlobalResponseHandler;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle EmailNotFoundException and return a 404 response with the error
    // message
    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEmailNotFoundException(EmailNotFoundException ex) {

        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        Map<String, String> response = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> response.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, String>> handleNullPointerException(NullPointerException ex) {
        logger.error("Null pointer exception occurred: ", ex);
        Map<String, String> response = new HashMap<>();
        response.put("message", "An unexpected error occurred. Please try again later.");
        return ResponseEntity.status(500).body(response);
    }

    // Handle EmailAlreadyExistsException and return a 400 response with the error
    // message
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        logger.warn("{}", ex.getMessage());
        return GlobalResponseHandler.error(
                ex.getMessage(),
                null,
                HttpStatus.CONFLICT);
    }

    // Handle BadCredentialsException and return a 401 response with the error
    // message
    @ExceptionHandler(PhoneNumberAlreadyExists.class)
    public ResponseEntity<ApiResponse<Object>> handlePhoneNumberAlreadyExists(PhoneNumberAlreadyExists ex) {
        logger.warn("{}", ex.getMessage());
        return GlobalResponseHandler.error(
                ex.getMessage(),
                null,
                HttpStatus.CONFLICT);
    }

    // Handles the Otp Token not found exception
    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleTokenNotFoundException(TokenNotFoundException ex) {
        logger.warn("{}", ex.getMessage());
        return GlobalResponseHandler.error(
                ex.getMessage(),
                null,
                HttpStatus.NOT_FOUND);
    }

    // Handles token already exiration exception
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiResponse<Object>> handleTokenExiredException(TokenExpiredException ex) {
        logger.warn("{}", ex.getMessage());
        return GlobalResponseHandler.error(
                ex.getMessage(),
                null,
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handelUnauthorizedException(UnauthorizedException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 401);
        response.put("error", "Unauthorized");
        response.put("message", ex.getMessage());
        response.put("timestamp", System.currentTimeMillis());

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
}