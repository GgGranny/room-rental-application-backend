package com.room_rental_backend.room_rental_application.services;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.room_rental_backend.room_rental_application.components.RoommateEventPublisher;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.RoommateProfileRequest;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.RoommateRequestCreateRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoomInterestResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoommateCandidateResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoommateMapResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoommateProfileResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.RoommateRequestResponse;
import com.room_rental_backend.room_rental_application.enums.Cleanliness;
import com.room_rental_backend.room_rental_application.enums.KycStatus;
import com.room_rental_backend.room_rental_application.enums.NotificationType;
import com.room_rental_backend.room_rental_application.enums.Roles;
import com.room_rental_backend.room_rental_application.enums.RoomStatus;
import com.room_rental_backend.room_rental_application.enums.RoomType;
import com.room_rental_backend.room_rental_application.enums.RoommateRequestStatus;
import com.room_rental_backend.room_rental_application.enums.SleepSchedule;
import com.room_rental_backend.room_rental_application.exceptions.ConflictException;
import com.room_rental_backend.room_rental_application.exceptions.ForbiddenException;
import com.room_rental_backend.room_rental_application.exceptions.UnauthorizedException;
import com.room_rental_backend.room_rental_application.exceptions.UserNotFoundException;
import com.room_rental_backend.room_rental_application.interfaces.NotificationService;
import com.room_rental_backend.room_rental_application.interfaces.RoommateService;
import com.room_rental_backend.room_rental_application.models.Kyc;
import com.room_rental_backend.room_rental_application.models.Room;
import com.room_rental_backend.room_rental_application.models.RoommateInterest;
import com.room_rental_backend.room_rental_application.models.RoommateProfile;
import com.room_rental_backend.room_rental_application.models.RoommateRequest;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.repositories.KycRepository;
import com.room_rental_backend.room_rental_application.repositories.RoomRepository;
import com.room_rental_backend.room_rental_application.repositories.RoommateInterestRepository;
import com.room_rental_backend.room_rental_application.repositories.RoommateProfileRepository;
import com.room_rental_backend.room_rental_application.repositories.RoommateRequestRepository;
import com.room_rental_backend.room_rental_application.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// New API: tenant Roommate Finder workflow. Business rules enforced here:
// - Only SHARED rooms participate; PRIVATE rooms are rejected at API level.
// - The room must still be AVAILABLE per the existing room availability rules.
// - Sending a roommate request requires APPROVED KYC (same rule as visits).
// - One active request per requester->recipient pair per shared room.
// - Notifications fire only after the database save succeeds.
@Service
@RequiredArgsConstructor
@Slf4j
public class RoommateServiceImplementation implements RoommateService {

    private final RoommateProfileRepository profileRepository;
    private final RoommateInterestRepository interestRepository;
    private final RoommateRequestRepository requestRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final KycRepository kycRepository;
    private final NotificationService notificationService;
    private final RoommateEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    @Override
    public RoommateProfileResponse getMyProfile(Authentication authentication) {
        Users user = resolveUser(authentication);
        return profileRepository.findByUser(user)
                .map(profile -> toProfileResponse(user, profile))
                .orElseGet(() -> new RoommateProfileResponse(null, user.getId(), displayName(user),
                        user.getProfilePictureUrl(), null, null, null, null, null, null, null, null));
    }

    // Upsert: the owner is always the authenticated user, never a client-supplied id.
    @Transactional
    @Override
    public RoommateProfileResponse saveProfile(RoommateProfileRequest request, Authentication authentication) {
        Users user = resolveUser(authentication);
        if (user.getRoles() != Roles.ROLE_USER) {
            throw new ForbiddenException("Only tenants can use the Roommate Finder");
        }
        RoommateProfile profile = profileRepository.findByUser(user).orElseGet(() ->
                RoommateProfile.builder().user(user).build());
        profile.setBio(trimToNull(request.bio()));
        profile.setBudget(request.budget());
        profile.setPreferredLocation(trimToNull(request.preferredLocation()));
        profile.setPreferredMoveIn(trimToNull(request.preferredMoveIn()));
        profile.setSmoker(request.smoker());
        profile.setPetsOk(request.petsOk());
        profile.setSleepSchedule(request.sleepSchedule());
        profile.setCleanliness(request.cleanliness());
        RoommateProfile saved = profileRepository.save(profile);
        return toProfileResponse(user, saved);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RoomInterestResponse> getMyInterests(Authentication authentication) {
        Users user = resolveUser(authentication);
        return interestRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toInterestResponse)
                .toList();
    }

    @Transactional
    @Override
    public RoomInterestResponse expressInterest(String roomId, Authentication authentication) {
        Users user = resolveUser(authentication);
        if (user.getRoles() != Roles.ROLE_USER) {
            throw new ForbiddenException("Only tenants can use the Roommate Finder");
        }
        Room room = requireSharedRoom(roomId);
        RoommateInterest interest = interestRepository.findByUserAndRoom(user, room)
                .orElseGet(() -> interestRepository.save(RoommateInterest.builder()
                        .user(user)
                        .room(room)
                        .build()));
        return toInterestResponse(interest);
    }

    @Transactional
    @Override
    public void removeInterest(String roomId, Authentication authentication) {
        Users user = resolveUser(authentication);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found for id: " + roomId));
        interestRepository.findByUserAndRoom(user, room)
                .ifPresent(interestRepository::delete);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RoommateCandidateResponse> getCandidatesForRoom(String roomId, Authentication authentication) {
        Users viewer = resolveUser(authentication);
        Room room = requireSharedRoom(roomId);
        RoommateProfile viewerProfile = profileRepository.findByUser(viewer).orElse(null);

        return interestRepository.findByRoom(room).stream()
                .filter(interest -> !interest.getUser().getId().equals(viewer.getId()))
                .map(RoommateInterest::getUser)
                // Every tenant interested in THIS room is discoverable — a missing
                // roommate profile only means fewer details on the card.
                .map(user -> {
                    RoommateProfile profile = profileRepository.findByUser(user).orElse(null);
                    return new RoommateCandidateResponse(
                            toProfileResponse(user, profile),
                            compatibilityScore(viewerProfile, profile));
                })
                .sorted((a, b) -> Integer.compare(b.compatibilityScore(), a.compatibilityScore()))
                .toList();
    }

    @Transactional
    @Override
    public RoommateRequestResponse sendRequest(RoommateRequestCreateRequest request, Authentication authentication) {
        Users requester = resolveUser(authentication);

        requireApprovedKyc(requester, "send a roommate request");

        if (request.recipientId().equals(requester.getId())) {
            throw new IllegalArgumentException("You cannot send a roommate request to yourself");
        }

        // Serialize concurrent request creations per room against availability changes.
        Room room = roomRepository.findByIdForUpdate(request.roomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found for id: " + request.roomId()));
        requireSharedRoom(room);
        requireAvailable(room);

        Users recipient = userRepository.findById(request.recipientId())
                .orElseThrow(() -> new UserNotFoundException("Recipient not found for id: " + request.recipientId()));
        if (recipient.getRoles() != Roles.ROLE_USER) {
            throw new IllegalArgumentException("Roommate requests can only be sent to tenants");
        }

        // The recipient must have expressed interest in this shared room so requests
        // always stay anchored to a real roommate opportunity.
        if (!interestRepository.existsByUserAndRoom(recipient, room)) {
            throw new IllegalArgumentException(
                    displayName(recipient) + " has not expressed interest in this shared room");
        }

        boolean duplicateActive = requestRepository.existsByRequesterAndRecipientAndRoomAndStatusIn(
                requester, recipient, room,
                List.of(RoommateRequestStatus.PENDING, RoommateRequestStatus.ACCEPTED));
        if (duplicateActive) {
            throw new IllegalArgumentException("Roommate request already sent.");
        }

        RoommateRequest saved = requestRepository.save(RoommateRequest.builder()
                .requester(requester)
                .recipient(recipient)
                .room(room)
                .message(trimToNull(request.message()))
                .status(RoommateRequestStatus.PENDING)
                .build());

        // Notify only after a successful save; FCM failures never roll this back.
        notificationService.sendToUser(recipient, "New Roommate Request",
                displayName(requester) + " sent you a roommate request for \"" + room.getRoomTitle() + "\".",
                NotificationType.ROOMMATE_REQUEST, saved.getId());

        // Real-time sync AFTER commit: refresh maps of other tenants viewing this room.
        eventPublisher.publish(room.getId(), RoommateEventPublisher.ROOMMATE_REQUEST_CREATED,
                saved.getId(), saved.getStatus().name());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RoommateRequestResponse> getSentRequests(Authentication authentication) {
        Users user = resolveUser(authentication);
        return requestRepository.findByRequesterOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<RoommateRequestResponse> getReceivedRequests(Authentication authentication) {
        Users user = resolveUser(authentication);
        return requestRepository.findByRecipientOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    // Acceptance is the critical race: A and C may both try to accept B's request.
    // Strategy: pessimistic lock on the request row, then the room row, inside one
    // transaction. The loser of the race sees a non-PENDING state and gets HTTP 409.
    @Transactional
    @Override
    public RoommateRequestResponse acceptRequest(String requestId, Authentication authentication) {
        Users acceptor = resolveUser(authentication);

        // KYC gate for acceptance too — always re-verified from the database here.
        requireApprovedKyc(acceptor, "accept a roommate request");

        RoommateRequest roommateRequest = requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Roommate request not found for id: " + requestId));

        // Ownership: only the receiving tenant may accept their own request.
        if (!roommateRequest.getRecipient().getId().equals(acceptor.getId())) {
            throw new UnauthorizedException("You can only accept your own received requests");
        }
        if (roommateRequest.getStatus() != RoommateRequestStatus.PENDING) {
            throw new ConflictException("This roommate request is no longer available.");
        }

        // Booking race: lock the room and re-check availability/capacity in the same tx.
        Room room = roomRepository.findByIdForUpdate(roommateRequest.getRoom().getId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        requireSharedRoom(room);
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new ConflictException("This shared room is no longer available.");
        }
        long acceptedCount = requestRepository.countByRoomAndStatus(room, RoommateRequestStatus.ACCEPTED);
        if (acceptedCount >= capacityOf(room)) {
            throw new ConflictException("Another roommate has already been accepted for this room.");
        }

        roommateRequest.setStatus(RoommateRequestStatus.ACCEPTED);
        RoommateRequest saved = requestRepository.save(roommateRequest);

        notificationService.sendToUser(saved.getRequester(), "Roommate Request Accepted",
                displayName(acceptor) + " accepted your roommate request for \"" + room.getRoomTitle() + "\".",
                NotificationType.ROOMMATE_REQUEST_ACCEPTED, saved.getId());

        // Real-time sync AFTER commit: every open map drops this opportunity.
        eventPublisher.publish(room.getId(), RoommateEventPublisher.ROOMMATE_REQUEST_ACCEPTED,
                saved.getId(), saved.getStatus().name());

        return toResponse(saved);
    }

    @Transactional
    @Override
    public RoommateRequestResponse rejectRequest(String requestId, Authentication authentication) {
        Users recipient = resolveUser(authentication);
        RoommateRequest roommateRequest = requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Roommate request not found for id: " + requestId));

        // Ownership: only the receiving tenant may reject their own request.
        if (!roommateRequest.getRecipient().getId().equals(recipient.getId())) {
            throw new UnauthorizedException("You can only respond to your own received requests");
        }
        if (roommateRequest.getStatus() != RoommateRequestStatus.PENDING) {
            throw new ConflictException("This roommate request is no longer available.");
        }

        roommateRequest.setStatus(RoommateRequestStatus.REJECTED);
        RoommateRequest saved = requestRepository.save(roommateRequest);

        String roomTitle = saved.getRoom() != null ? saved.getRoom().getRoomTitle() : "the shared room";
        notificationService.sendToUser(saved.getRequester(), "Roommate Request Rejected",
                displayName(recipient) + " rejected your roommate request for \"" + roomTitle + "\".",
                NotificationType.ROOMMATE_REQUEST_REJECTED, saved.getId());

        eventPublisher.publish(saved.getRoom().getId(), RoommateEventPublisher.ROOMMATE_REQUEST_REJECTED,
                saved.getId(), saved.getStatus().name());

        return toResponse(saved);
    }

    @Transactional
    @Override
    public RoommateRequestResponse cancelRequest(String requestId, Authentication authentication) {
        Users requester = resolveUser(authentication);
        RoommateRequest roommateRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Roommate request not found for id: " + requestId));

        // Ownership: only the sender may cancel, and only while still pending.
        if (!roommateRequest.getRequester().getId().equals(requester.getId())) {
            throw new UnauthorizedException("You can only cancel your own sent requests");
        }
        if (roommateRequest.getStatus() != RoommateRequestStatus.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be cancelled");
        }

        roommateRequest.setStatus(RoommateRequestStatus.CANCELLED);
        RoommateRequest saved = requestRepository.save(roommateRequest);

        eventPublisher.publish(saved.getRoom().getId(), RoommateEventPublisher.ROOMMATE_REQUEST_CANCELLED,
                saved.getId(), saved.getStatus().name());

        return toResponse(saved);
    }

    // --- helpers -----------------------------------------------------------

    private Users resolveUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));
    }

    // KYC gate mirrors the viewing-schedule rule; Admin is exempt.
    private void requireApprovedKyc(Users user, String action) {
        if (user.getRoles() == Roles.ROLE_ADMIN) {
            return;
        }
        KycStatus kycStatus = kycRepository.findByUserId(user.getId())
                .map(Kyc::getStatus)
                .orElse(null);
        if (kycStatus == null) {
            throw new ForbiddenException("KYC verification is required before you can " + action
                    + ". Please complete your KYC.");
        }
        if (kycStatus == KycStatus.PENDING) {
            throw new ForbiddenException(
                    "Your KYC is currently under review. You can " + action + " after your KYC is approved.");
        }
        if (kycStatus == KycStatus.REJECTED) {
            throw new ForbiddenException(
                    "Your KYC was rejected. Please resubmit your KYC before you can " + action + ".");
        }
        if (kycStatus != KycStatus.APPROVED) {
            throw new ForbiddenException("KYC verification is required before you can " + action + ".");
        }
    }

    // The Roommate Finder only works for SHARED rooms.
    private Room requireSharedRoom(String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found for id: " + roomId));
        return requireSharedRoom(room);
    }

    private Room requireSharedRoom(Room room) {
        if (room.getSharingType() != RoomType.SHARED) {
            throw new IllegalArgumentException("Roommate finder is only available for shared rooms");
        }
        return room;
    }

    // Capacity/availability rule reuses the existing room status field — a shared
    // room that is no longer AVAILABLE has no free capacity for new roommates.
    private void requireAvailable(Room room) {
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new ConflictException("This shared room is no longer available.");
        }
    }

    // Capacity reuses the existing totalRooms field (no second capacity system):
    // each accepted arrangement fills one slot; default to a single slot when unset.
    private int capacityOf(Room room) {
        Integer totalRooms = room.getTotalRooms();
        return totalRooms == null || totalRooms < 1 ? 1 : totalRooms;
    }

    // New API: single payload for the shared-room roommate map. Marker positions are
    // derived client-side from the ROOM's own coordinates — tenant home locations are
    // never stored or exposed. Opportunities exclude the viewer, already-matched
    // tenants and (entirely) unavailable/private rooms.
    @Transactional(readOnly = true)
    @Override
    public RoommateMapResponse getMapForRoom(String roomId, Authentication authentication) {
        Users viewer = resolveUser(authentication);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found for id: " + roomId));

        boolean openForMatching = room.getSharingType() == RoomType.SHARED
                && room.getStatus() == RoomStatus.AVAILABLE;

        List<RoommateMapResponse.Opportunity> opportunities;
        if (!openForMatching) {
            opportunities = List.of();
        } else {
            RoommateProfile viewerProfile = profileRepository.findByUser(viewer).orElse(null);

            // Tenants already matched on this room are hidden from discovery.
            Set<String> matchedUserIds = new HashSet<>();
            for (RoommateRequest accepted : requestRepository.findByRoomAndStatus(room, RoommateRequestStatus.ACCEPTED)) {
                matchedUserIds.add(accepted.getRequester().getId());
                matchedUserIds.add(accepted.getRecipient().getId());
            }

            // Pending requests waiting for ME on this room (marker card shows Accept).
            Map<String, String> incomingPending = new HashMap<>();
            for (RoommateRequest incoming : requestRepository.findByRecipientOrderByCreatedAtDesc(viewer)) {
                if (room.getId().equals(incoming.getRoom().getId())
                        && incoming.getStatus() == RoommateRequestStatus.PENDING) {
                    incomingPending.putIfAbsent(incoming.getRequester().getId(), incoming.getId());
                }
            }

            opportunities = interestRepository.findByRoom(room).stream()
                    .filter(interest -> !interest.getUser().getId().equals(viewer.getId()))
                    .map(RoommateInterest::getUser)
                    .filter(user -> !matchedUserIds.contains(user.getId()))
                    // Every interested tenant appears on the map; a missing profile
                    // only means fewer details on the marker card.
                    .map(user -> toOpportunity(user,
                            profileRepository.findByUser(user).orElse(null),
                            viewer, viewerProfile, room, incomingPending))
                    .sorted((a, b) -> Integer.compare(b.compatibilityScore(), a.compatibilityScore()))
                    .toList();
        }

        return new RoommateMapResponse(
                new RoommateMapResponse.RoomSummary(room.getId(), room.getRoomTitle(), room.getPrice(),
                        room.getLocation(), room.getLatitude(), room.getLongitude(), room.getStatus(),
                        room.getSharingType()),
                opportunities);
    }

    private RoommateMapResponse.Opportunity toOpportunity(Users owner, RoommateProfile profile,
            Users viewer, RoommateProfile viewerProfile, Room room, Map<String, String> incomingPending) {
        RoommateRequestStatus myStatus = requestRepository
                .findByRequesterAndRecipientAndRoomOrderByCreatedAtDesc(viewer, owner, room).stream()
                .findFirst()
                .map(RoommateRequest::getStatus)
                .orElse(null);
        return new RoommateMapResponse.Opportunity(
                owner.getId(),
                displayName(owner),
                owner.getProfilePictureUrl(),
                profile != null ? profile.getBudget() : null,
                profile != null ? profile.getBio() : null,
                compatibilityScore(viewerProfile, profile),
                myStatus,
                incomingPending.get(owner.getId()));
    }

    // MVP preference matching (NOT AI): budget 40%, lifestyle 40%, move-in 20%.
    private int compatibilityScore(RoommateProfile a, RoommateProfile b) {
        int score = 0;
        score += budgetScore(a == null ? null : a.getBudget(), b == null ? null : b.getBudget());
        score += lifestyleScore(a, b);
        score += moveInScore(a == null ? null : a.getPreferredMoveIn(), b == null ? null : b.getPreferredMoveIn());
        return Math.max(0, Math.min(100, score));
    }

    private int budgetScore(BigDecimal a, BigDecimal b) {
        if (a == null || b == null || a.signum() <= 0 || b.signum() <= 0) {
            return 20; // unknown budgets are neutral
        }
        double diff = Math.abs(a.doubleValue() - b.doubleValue());
        double ratio = diff / Math.max(a.doubleValue(), b.doubleValue());
        if (ratio <= 0.1) {
            return 40;
        }
        if (ratio <= 0.25) {
            return 30;
        }
        if (ratio <= 0.5) {
            return 15;
        }
        return 0;
    }

    private int lifestyleScore(RoommateProfile a, RoommateProfile b) {
        int score = 0;
        score += boolMatches(a == null ? null : a.getSmoker(), b == null ? null : b.getSmoker()) ? 10 : 0;
        score += boolMatches(a == null ? null : a.getPetsOk(), b == null ? null : b.getPetsOk()) ? 10 : 0;
        SleepSchedule sa = a == null ? null : a.getSleepSchedule();
        SleepSchedule sb = b == null ? null : b.getSleepSchedule();
        score += sa != null && sb != null && sa == sb ? 10 : 0;
        Cleanliness ca = a == null ? null : a.getCleanliness();
        Cleanliness cb = b == null ? null : b.getCleanliness();
        score += ca != null && cb != null && ca == cb ? 10 : 0;
        return score;
    }

    private boolean boolMatches(Boolean a, Boolean b) {
        return a != null && b != null && a.booleanValue() == b.booleanValue();
    }

    private int moveInScore(String a, String b) {
        String left = a == null ? "" : a.trim().toLowerCase();
        String right = b == null ? "" : b.trim().toLowerCase();
        if (left.isEmpty() && right.isEmpty()) {
            return 20; // both flexible
        }
        if (left.isEmpty() || right.isEmpty()) {
            return 10; // one flexible
        }
        return left.equals(right) ? 20 : 0;
    }

    private RoomInterestResponse toInterestResponse(RoommateInterest interest) {
        Room room = interest.getRoom();
        return new RoomInterestResponse(room.getId(), room.getRoomTitle(), room.getLocation(),
                room.getPrice(), room.getStatus(), room.getSharingType(), interest.getCreatedAt());
    }

    // Builds the profile view from the user plus their OPTIONAL roommate profile —
    // tenants without one still appear in discovery with identity info only.
    private RoommateProfileResponse toProfileResponse(Users user, RoommateProfile profile) {
        return new RoommateProfileResponse(profile != null ? profile.getId() : null, user.getId(),
                displayName(user), user.getProfilePictureUrl(), profile != null ? profile.getBio() : null,
                profile != null ? profile.getBudget() : null,
                profile != null ? profile.getPreferredLocation() : null,
                profile != null ? profile.getPreferredMoveIn() : null,
                profile != null ? profile.getSmoker() : null,
                profile != null ? profile.getPetsOk() : null,
                profile != null ? profile.getSleepSchedule() : null,
                profile != null ? profile.getCleanliness() : null);
    }

    private RoommateRequestResponse toResponse(RoommateRequest request) {
        Room room = request.getRoom();
        var property = room.getProperty();
        return new RoommateRequestResponse(request.getId(), room.getId(), room.getRoomTitle(),
                property != null ? property.getPropertyName() : null, room.getPrice(),
                new RoommateRequestResponse.UserSummary(request.getRequester().getId(),
                        displayName(request.getRequester()), request.getRequester().getProfilePictureUrl()),
                new RoommateRequestResponse.UserSummary(request.getRecipient().getId(),
                        displayName(request.getRecipient()), request.getRecipient().getProfilePictureUrl()),
                request.getStatus(), request.getMessage(), request.getCreatedAt(), request.getUpdatedAt());
    }

    private String displayName(Users user) {
        if (user == null) {
            return "A tenant";
        }
        String first = user.getFname() == null ? "" : user.getFname();
        String last = user.getLname() == null ? "" : user.getLname();
        String name = (first + " " + last).trim();
        return name.isEmpty() ? user.getEmail() : name;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
