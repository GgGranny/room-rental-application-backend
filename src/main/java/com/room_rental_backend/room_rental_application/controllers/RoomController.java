package com.room_rental_backend.room_rental_application.controllers;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.room_rental_backend.room_rental_application.interfaces.RoomService;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.RoomRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoomDetailsResponseDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoomResponseDto;
import com.room_rental_backend.room_rental_application.models.filters.RoomSearchFilter;
import com.room_rental_backend.room_rental_application.responseHandler.ApiResponse;
import com.room_rental_backend.room_rental_application.responseHandler.GlobalResponseHandler;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

        private final RoomService roomsService;

        private final ObjectMapper objectMapper;

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiResponse<RoomDetailsResponseDto>> createRoom(
                        @Valid @RequestPart("roomData") String roomRequest,
                        @RequestPart(value = "roomImages", required = false) List<MultipartFile> roomImages) {
                RoomRequest request = objectMapper.readValue(roomRequest, RoomRequest.class);
                RoomDetailsResponseDto response = roomsService.createRoom(request, roomImages);
                return GlobalResponseHandler.success(
                                "Room created successfully",
                                response,
                                HttpStatus.CREATED);
        }

        @GetMapping("/{roomId}")
        public ResponseEntity<ApiResponse<RoomDetailsResponseDto>> getRoomDetails(
                        @PathVariable("roomId") String roomId) {
                RoomDetailsResponseDto response = roomsService.getRoomDetailsForOne(roomId);
                return GlobalResponseHandler.success(
                                "Room details fetched successfully",
                                response,
                                HttpStatus.OK);
        }

        @GetMapping
        public ResponseEntity<ApiResponse<List<RoomResponseDto>>> getRecommendedRooms() {
                List<RoomResponseDto> response = roomsService.getAllRecommendedRooms();
                return GlobalResponseHandler.success(
                                "Rooms fetched successfully",
                                response,
                                HttpStatus.OK);
        }

        @GetMapping("/featured")
        public ResponseEntity<ApiResponse<List<RoomResponseDto>>> getFeaturedRooms(
                        @RequestParam("roomIds") List<String> roomIds) {
                List<RoomResponseDto> response = roomsService.getAllFeaturedRooms(roomIds);
                return GlobalResponseHandler.success(
                                "Featured rooms fetched successfully",
                                response,
                                HttpStatus.OK);
        }

        @GetMapping("/search")
        public ResponseEntity<ApiResponse<List<RoomResponseDto>>> searchRooms(
                        @RequestParam(value = "location", required = false) String location,
                        @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
                        @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
                        @RequestParam(value = "roomType", required = false) String roomType) {
                RoomSearchFilter filter = RoomSearchFilter.builder()
                                .location(location)
                                .minPrice(minPrice)
                                .maxPrice(maxPrice)
                                .roomType(roomType)
                                .build();
                List<RoomResponseDto> response = roomsService.searchRooms(filter);
                return GlobalResponseHandler.success(
                                "Rooms searched successfully",
                                response,
                                HttpStatus.OK);
        }

        @PutMapping(value = "/{roomId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiResponse<RoomDetailsResponseDto>> updateRoom(
                        @PathVariable("roomId") String roomId,
                        @Valid @RequestPart("roomData") String roomRequest,
                        @RequestPart(value = "roomImages", required = false) List<MultipartFile> roomImages,
                        @RequestParam(value = "roomIdsToRemove", required = false) List<Long> roomIdsToRemove) {
                RoomRequest request = objectMapper.readValue(roomRequest, RoomRequest.class);
                RoomDetailsResponseDto response = roomsService.updateRoom(roomId, request, roomImages, roomIdsToRemove);
                return GlobalResponseHandler.success(
                                "Room updated successfully",
                                response,
                                HttpStatus.OK);
        }

        @PatchMapping("/{roomId}/status/{status}")
        public ResponseEntity<ApiResponse<RoomDetailsResponseDto>> updateRoomStatus(
                        @PathVariable("roomId") String roomId,
                        @PathVariable("status") String status) {
                RoomDetailsResponseDto response = roomsService.updatedRoomStatus(roomId, status);
                return GlobalResponseHandler.success(
                                "Room status updated successfully",
                                response,
                                HttpStatus.OK);
        }

        @DeleteMapping("/{roomId}")
        public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable("roomId") String roomId) {
                roomsService.deleteRoom(roomId);
                return GlobalResponseHandler.success(
                                "Room deleted successfully",
                                null,
                                HttpStatus.OK);
        }
}
