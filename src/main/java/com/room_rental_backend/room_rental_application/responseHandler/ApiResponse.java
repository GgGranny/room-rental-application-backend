package com.room_rental_backend.room_rental_application.responseHandler;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {

    private HttpStatus status;

    private boolean success;

    private String message;

    private T data;

    private LocalDateTime timeStamp;

}
