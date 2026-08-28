package com.room_rental_backend.room_rental_application.interfaces;

import java.util.List;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.RoommateProfileRequest;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.RoommateRequestCreateRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoomInterestResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoommateCandidateResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoommateMapResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoommateProfileResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoommateRequestResponse;

// New API: tenant-side Roommate Finder operations. All ownership comes from the
// authenticated principal; no user ids are trusted from the client.
public interface RoommateService {

    RoommateProfileResponse getMyProfile(org.springframework.security.core.Authentication authentication);

    RoommateProfileResponse saveProfile(RoommateProfileRequest request,
            org.springframework.security.core.Authentication authentication);

    List<RoomInterestResponse> getMyInterests(org.springframework.security.core.Authentication authentication);

    RoomInterestResponse expressInterest(String roomId, org.springframework.security.core.Authentication authentication);

    void removeInterest(String roomId, org.springframework.security.core.Authentication authentication);

    List<RoommateCandidateResponse> getCandidatesForRoom(String roomId,
            org.springframework.security.core.Authentication authentication);

    RoommateMapResponse getMapForRoom(String roomId,
            org.springframework.security.core.Authentication authentication);

    RoommateRequestResponse sendRequest(RoommateRequestCreateRequest request,
            org.springframework.security.core.Authentication authentication);

    List<RoommateRequestResponse> getSentRequests(org.springframework.security.core.Authentication authentication);

    List<RoommateRequestResponse> getReceivedRequests(org.springframework.security.core.Authentication authentication);

    // The counterpart tenant's roommate profile (+ compatibility) for a request the
    // authenticated user is a participant of. Powers the "click a request → view the
    // tenant's details" step before accepting. Non-participants are rejected (403).
    RoommateCandidateResponse getRequestCounterpartProfile(String requestId,
            org.springframework.security.core.Authentication authentication);

    RoommateRequestResponse acceptRequest(String requestId, org.springframework.security.core.Authentication authentication);

    RoommateRequestResponse rejectRequest(String requestId, org.springframework.security.core.Authentication authentication);

    RoommateRequestResponse cancelRequest(String requestId, org.springframework.security.core.Authentication authentication);
}
