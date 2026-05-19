package com.room_rental_backend.room_rental_application.responseHandler;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class GlobalResponseHandler {

    // Handler for the success response
    public static <T> ResponseEntity<ApiResponse<T>> success (String message, T data , HttpStatus status){
        ApiResponse<T> response = new ApiResponse<>(
                status,
                true,
                message,
                data,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, status);
    }

    //handler for the error response
    public static <T> ResponseEntity<ApiResponse<T>> error(String message, T data, HttpStatus status) {
        ApiResponse<T> response = new ApiResponse<>(
                status,
                false,
                message,
                data,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, status);
    }
}
