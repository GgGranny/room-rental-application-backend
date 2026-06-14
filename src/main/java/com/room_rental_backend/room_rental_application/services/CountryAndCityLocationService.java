package com.room_rental_backend.room_rental_application.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.room_rental_backend.room_rental_application.responseHandler.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class CountryAndCityLocationService {

    private final RestTemplate restTemplate;

    @Value("${city.and.country.location.baseUrl}")
    private String apiUrl;

    private final ObjectMapper objectMapper;

    public ApiResponse<Object> getCountries() {
        try {
            ResponseEntity<Object> response = this.restTemplate.getForEntity(apiUrl, Object.class);
            if (response == null) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Countries not found");
            }
            Map<String, Object> objMap = objectMapper.convertValue(response.getBody(), Map.class);
            return ApiResponse.builder()
                    .data(objMap.get("data"))
                    .message("Data retrived successfully")
                    .status(HttpStatus.OK)
                    .build();
        } catch (HttpClientErrorException e) {
            log.warn("failed to fetch the countries: {}", apiUrl);
            return ApiResponse.builder()
                    .data(null)
                    .message(e.getMessage())
                    .status(HttpStatus.valueOf(e.getStatusCode().value()))
                    .build();
        } catch (HttpServerErrorException e) {
            log.warn("Client error: {}", apiUrl);
            log.warn("status: {}", e.getStatusCode());
            return ApiResponse.builder()
                    .data(null)
                    .message(e.getMessage())
                    .status(HttpStatus.valueOf(e.getStatusCode().value()))
                    .build();
        } catch (ResourceAccessException e) {
            log.warn(apiUrl, e.getMessage());
            return ApiResponse.builder()
                    .data(null)
                    .message(e.getMessage())
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .build();
        } catch (RestClientException e) {
            log.warn(apiUrl, e.getMessage());
            return ApiResponse.builder()
                    .data(null)
                    .message(e.getMessage())
                    .status(HttpStatus.BAD_GATEWAY)
                    .build();
        }
    }
}
