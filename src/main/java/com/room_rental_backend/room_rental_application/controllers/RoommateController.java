package com.room_rental_backend.room_rental_application.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.RoommateProfileRequest;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.RoommateRequestCreateRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoomInterestResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoommateCandidateResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoommateMapResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoommateProfileResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoommateRequestResponse;
import com.room_rental_backend.room_rental_application.interfaces.RoommateService;
import com.room_rental_backend.room_rental_application.responseHandler.ApiResponse;
import com.room_rental_backend.room_rental_application.responseHandler.GlobalResponseHandler;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// New API: tenant Roommate Finder endpoints. Path security (see SpringSecurity)
// restricts /api/v1/roommates/** to ROLE_USER so landlords cannot act as
// tenants here; the service re-checks ownership on every mutation.
@RestController
@RequestMapping("/api/v1/roommates")
@RequiredArgsConstructor
public class RoommateController {

    private final RoommateService roommateService;

    // Tenant: my roommate profile (null data when not created yet).
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<RoommateProfileResponse>> getMyProfile(Authentication authentication) {
        return GlobalResponseHandler.success("Roommate profile fetched successfully",
                roommateService.getMyProfile(authentication), HttpStatus.OK);
    }

    // Tenant: create my roommate profile.
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<RoommateProfileResponse>> createProfile(
            @Valid @RequestBody RoommateProfileRequest request,
            Authentication authentication) {
        return GlobalResponseHandler.success("Roommate profile created successfully",
                roommateService.saveProfile(request, authentication), HttpStatus.CREATED);
    }

    // Tenant: update my roommate profile.
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<RoommateProfileResponse>> updateProfile(
            @Valid @RequestBody RoommateProfileRequest request,
            Authentication authentication) {
        return GlobalResponseHandler.success("Roommate profile updated successfully",
                roommateService.saveProfile(request, authentication), HttpStatus.OK);
    }

    // Tenant: shared rooms I have expressed interest in.
    @GetMapping("/interests")
    public ResponseEntity<ApiResponse<List<RoomInterestResponse>>> getMyInterests(Authentication authentication) {
        return GlobalResponseHandler.success("Roommate interests fetched successfully",
                roommateService.getMyInterests(authentication), HttpStatus.OK);
    }

    // Tenant: "I am interested in sharing this room."
    @PostMapping("/interests/{roomId}")
    public ResponseEntity<ApiResponse<RoomInterestResponse>> expressInterest(
            @PathVariable("roomId") String roomId,
            Authentication authentication) {
        return GlobalResponseHandler.success("Interest registered successfully",
                roommateService.expressInterest(roomId, authentication), HttpStatus.CREATED);
    }

    // Tenant: withdraw interest in a shared room.
    @DeleteMapping("/interests/{roomId}")
    public ResponseEntity<ApiResponse<Void>> removeInterest(
            @PathVariable("roomId") String roomId,
            Authentication authentication) {
        roommateService.removeInterest(roomId, authentication);
        return GlobalResponseHandler.success("Interest removed successfully", null, HttpStatus.OK);
    }

    // Tenant: candidate roommates for a shared room (with compatibility score).
    @GetMapping("/rooms/{roomId}/candidates")
    public ResponseEntity<ApiResponse<List<RoommateCandidateResponse>>> getCandidates(
            @PathVariable("roomId") String roomId,
            Authentication authentication) {
        return GlobalResponseHandler.success("Roommate candidates fetched successfully",
                roommateService.getCandidatesForRoom(roomId, authentication), HttpStatus.OK);
    }

    // New API: map payload for a shared room — room summary + active opportunities.
    // Marker positions are derived on the client from the room's own coordinates;
    // no tenant personal locations are exposed.
    @GetMapping("/rooms/{roomId}/map")
    public ResponseEntity<ApiResponse<RoommateMapResponse>> getMap(
            @PathVariable("roomId") String roomId,
            Authentication authentication) {
        return GlobalResponseHandler.success("Roommate map fetched successfully",
                roommateService.getMapForRoom(roomId, authentication), HttpStatus.OK);
    }

    // Tenant: send a roommate request for a shared room.
    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<RoommateRequestResponse>> sendRequest(
            @Valid @RequestBody RoommateRequestCreateRequest request,
            Authentication authentication) {
        return GlobalResponseHandler.success("Roommate request sent successfully",
                roommateService.sendRequest(request, authentication), HttpStatus.CREATED);
    }

    // Tenant: requests I have sent.
    @GetMapping("/requests/sent")
    public ResponseEntity<ApiResponse<List<RoommateRequestResponse>>> getSentRequests(Authentication authentication) {
        return GlobalResponseHandler.success("Sent roommate requests fetched successfully",
                roommateService.getSentRequests(authentication), HttpStatus.OK);
    }

    // Tenant: requests I have received.
    @GetMapping("/requests/received")
    public ResponseEntity<ApiResponse<List<RoommateRequestResponse>>> getReceivedRequests(
            Authentication authentication) {
        return GlobalResponseHandler.success("Received roommate requests fetched successfully",
                roommateService.getReceivedRequests(authentication), HttpStatus.OK);
    }

    // Recipient: accept a received pending request.
    @PatchMapping("/requests/{id}/accept")
    public ResponseEntity<ApiResponse<RoommateRequestResponse>> acceptRequest(
            @PathVariable("id") String id,
            Authentication authentication) {
        return GlobalResponseHandler.success("Roommate request accepted successfully",
                roommateService.acceptRequest(id, authentication), HttpStatus.OK);
    }

    // Recipient: reject a received pending request.
    @PatchMapping("/requests/{id}/reject")
    public ResponseEntity<ApiResponse<RoommateRequestResponse>> rejectRequest(
            @PathVariable("id") String id,
            Authentication authentication) {
        return GlobalResponseHandler.success("Roommate request rejected successfully",
                roommateService.rejectRequest(id, authentication), HttpStatus.OK);
    }

    // Requester: cancel my own pending request.
    @PatchMapping("/requests/{id}/cancel")
    public ResponseEntity<ApiResponse<RoommateRequestResponse>> cancelRequest(
            @PathVariable("id") String id,
            Authentication authentication) {
        return GlobalResponseHandler.success("Roommate request cancelled successfully",
                roommateService.cancelRequest(id, authentication), HttpStatus.OK);
    }
}
