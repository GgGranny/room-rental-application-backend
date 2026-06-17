package com.room_rental_backend.room_rental_application.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.PropertyRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.PropertyDetailsResponseDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.PropertyResponseDto;
import com.room_rental_backend.room_rental_application.interfaces.PropertyService;
import com.room_rental_backend.room_rental_application.responseHandler.ApiResponse;
import com.room_rental_backend.room_rental_application.responseHandler.GlobalResponseHandler;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/property")
@RequiredArgsConstructor
public class PropertyController {

        private final PropertyService propertyService;

        private final ObjectMapper objectMapper;

        @GetMapping("/test")
        public String getMethodName() {
                return "hello there ";
        }

        // @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        // public ResponseEntity<ApiResponse<PropertyResponseDto>> createProperty(
        // @Valid @RequestPart("propertyData") PropertyRequest propertyRequest,
        // @RequestPart(value = "propertyThumbnail", required = false) MultipartFile
        // propertyThumbnail) {
        // PropertyResponseDto response =
        // propertyService.createProperty(propertyRequest, propertyThumbnail);
        // return GlobalResponseHandler.success(
        // "Property created successfully",
        // response,
        // HttpStatus.CREATED);
        // }

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiResponse<PropertyResponseDto>> createProperty(
                        @Valid @RequestPart("propertyData") String propertyRequest,
                        @RequestPart(value = "propertyThumbnail", required = false) MultipartFile propertyThumbnail) {
                PropertyRequest req = objectMapper.readValue(propertyRequest, PropertyRequest.class);
                PropertyResponseDto response = propertyService.createProperty(req, propertyThumbnail);
                return GlobalResponseHandler.success(
                                "Property created successfully",
                                response,
                                HttpStatus.CREATED);
        }

        @GetMapping
        public ResponseEntity<ApiResponse<List<PropertyResponseDto>>> fetchAllProperties() {
                List<PropertyResponseDto> response = propertyService.fetchAllProperties();
                return GlobalResponseHandler.success(
                                "Properties fetched successfully",
                                response,
                                HttpStatus.OK);
        }

        @GetMapping("/{propertyId}")
        public ResponseEntity<ApiResponse<PropertyDetailsResponseDto>> fetchPropertyDetails(
                        @PathVariable("propertyId") String propertyId) {
                PropertyDetailsResponseDto response = propertyService.fetchPropertyDetails(propertyId);
                return GlobalResponseHandler.success(
                                "Property details fetched successfully",
                                response,
                                HttpStatus.OK);
        }

        @PutMapping(value = "/{propertyId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiResponse<PropertyResponseDto>> updateProperty(
                        @PathVariable("propertyId") String propertyId,
                        @Valid @RequestPart("propertyData") String propertyRequest,
                        @RequestPart(value = "propertyThumbnail", required = false) MultipartFile propertyThumbnail) {
                PropertyRequest req = objectMapper.readValue(propertyRequest, PropertyRequest.class);
                PropertyResponseDto response = propertyService.updateProperty(propertyId, req,
                                propertyThumbnail);
                return GlobalResponseHandler.success(
                                "Property updated successfully",
                                response,
                                HttpStatus.OK);
        }

        @PatchMapping("/{propertyId}/status/{status}")
        public ResponseEntity<ApiResponse<PropertyResponseDto>> updatePropertyStatus(
                        @PathVariable("propertyId") String propertyId,
                        @PathVariable("status") String status) {
                PropertyResponseDto response = propertyService.updatedPropertyStatus(propertyId, status);
                return GlobalResponseHandler.success(
                                "Property status updated successfully",
                                response,
                                HttpStatus.OK);
        }

        @DeleteMapping("/{propertyId}")
        public ResponseEntity<ApiResponse<Void>> deleteProperty(@PathVariable("propertyId") String propertyId) {
                propertyService.deleteProperty(propertyId);
                return GlobalResponseHandler.success(
                                "Property deleted successfully",
                                null,
                                HttpStatus.OK);
        }
}
