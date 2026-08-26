package com.room_rental_backend.room_rental_application.services;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.DocumentDataReqeust;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.KycRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.KycResponse;
import com.room_rental_backend.room_rental_application.enums.ImageMetadataTypes;
import com.room_rental_backend.room_rental_application.enums.KycStatus;
import com.room_rental_backend.room_rental_application.enums.NotificationType;
import com.room_rental_backend.room_rental_application.exceptions.KycFailedException;
import com.room_rental_backend.room_rental_application.exceptions.UserNotFoundException;
import com.room_rental_backend.room_rental_application.interfaces.FileService;
import com.room_rental_backend.room_rental_application.interfaces.KycService;
import com.room_rental_backend.room_rental_application.interfaces.NotificationService;
import com.room_rental_backend.room_rental_application.interfaces.SupabaseFileStorageService;
import com.room_rental_backend.room_rental_application.interfaces.UserService;
import com.room_rental_backend.room_rental_application.mappers.KycMapper;
import com.room_rental_backend.room_rental_application.mappers.UserMapper;
import com.room_rental_backend.room_rental_application.models.Kyc;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.repositories.ImageMetadataRepository;
import com.room_rental_backend.room_rental_application.repositories.KycRepository;
import com.room_rental_backend.room_rental_application.repositories.UserRepository;

import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycSerciveImplementation implements KycService {

    @Value("${file.kyc.folder}")
    private String documentContainer;

    private final KycRepository kycRepository;

    private final FileService fileService;

    private final KycMapper kycMapper;

    private final UserRepository userRepository;

    private final ObjectMapper objectMapper;

    private final Validator validator;

    private final UserService userService;

    private final UserMapper userMapper;

    private final SupabaseFileStorageService supabaseFileStorageService;

    private final ImageMetadataRepository imageMetadataRepository;

    private final NotificationService notificationService;

    @PostConstruct()
    void init() throws IOException {
        File file = new File(documentContainer);
        try {
            if (file.exists()) {
                FileUtils.deleteDirectory(file);
            }
            boolean success = file.mkdirs();
            if (success) {
                System.out.println(documentContainer + " folder successfully created");
            }
        } catch (IOException e) {
            throw new IOException("Fialed to create the folder");
        }
    }

    // State machine: no record -> create PENDING; PENDING/APPROVED -> duplicate
    // rejected; REJECTED -> resubmit on the SAME record (REJECTED -> PENDING).
    // The KYC owner is always resolved from the authenticated principal — the
    // customerId sent by the client is never trusted.
    @Override
    public KycResponse submitKyc(String request, MultipartFile frontImage, MultipartFile backImage,
            MultipartFile selfie, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotFoundException("User is not authenticated");
        }
        KycRequest kycData = readAndValidateKycData(request);
        Users kycOwner = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));
        kycData.setCustomerId(kycOwner.getId());

        Kyc existingKyc = kycRepository.findByUserId(kycOwner.getId()).orElse(null);
        boolean resubmission = existingKyc != null;
        if (resubmission) {
            KycStatus currentStatus = existingKyc.getStatus();
            if (currentStatus == KycStatus.PENDING) {
                throw new KycFailedException("Your KYC is already under review");
            }
            if (currentStatus == KycStatus.APPROVED) {
                throw new KycFailedException("Your KYC has already been approved");
            }
            // REJECTED falls through: resubmission is allowed.
        }

        // Upload the replacement documents first so a failed upload can never
        // leave the existing submission broken or without its stored files.
        String frontImageUrl = supabaseFileStorageService.uploadFile(frontImage, "kyc", "private", "KYC").getUrl();
        String backImageUrl = backImage != null && !backImage.isEmpty()
                ? supabaseFileStorageService.uploadFile(backImage, "kyc", "private", "KYC").getUrl()
                : null;
        String selfieUrl = supabaseFileStorageService.uploadFile(selfie, "kyc", "private", "KYC").getUrl();

        DocumentDataReqeust documentDataReqeust = DocumentDataReqeust.builder()
                .documentType(kycData.getDocument().getDocumentType())
                .frontImageUrl(frontImageUrl)
                .backImageUrl(backImageUrl)
                .selfieUrl(selfieUrl)
                .build();
        kycData.setDocument(documentDataReqeust);

        Kyc savedKyc;
        if (resubmission) {
            List<String> replacedUrls = Stream.of(
                    existingKyc.getFrontImageUrl(),
                    existingKyc.getBackImageUrl(),
                    existingKyc.getSelfieUrl())
                    .filter(path -> path != null && !path.isEmpty())
                    .collect(Collectors.toList());
            kycMapper.updateEntity(existingKyc, kycData);
            existingKyc.setFrontImageUrl(frontImageUrl);
            existingKyc.setBackImageUrl(backImageUrl);
            existingKyc.setSelfieUrl(selfieUrl);
            existingKyc.setStatus(KycStatus.PENDING);
            existingKyc.setSubmittedAt(LocalDateTime.now());
            savedKyc = kycRepository.save(existingKyc);
            // Best-effort cleanup of the replaced documents after the record is safe.
            deleteReplacedKycDocuments(kycOwner.getId(), replacedUrls);
        } else {
            Kyc newKyc = kycMapper.toEntity(kycData);
            newKyc.setUser(kycOwner);
            newKyc.setStatus(KycStatus.PENDING);
            savedKyc = kycRepository.save(newKyc);
        }

        if (savedKyc != null) {
            return kycMapper.toResponse(savedKyc);
        }
        return KycResponse.builder()
                .customerId(kycOwner.getId())
                .build();
    }

    // KYC documents are tracked as ImageMetadata rows (type KYC); delete the rows
    // matching the replaced URLs, then remove the underlying storage objects.
    private void deleteReplacedKycDocuments(String userId, List<String> replacedUrls) {
        if (replacedUrls.isEmpty()) {
            return;
        }
        try {
            imageMetadataRepository.findAllByUserIdAndMetadataTypeAndUrlIn(userId, ImageMetadataTypes.KYC, replacedUrls)
                    .forEach(metadata -> supabaseFileStorageService.deleteFile(metadata.getId(), "private"));
        } catch (RuntimeException ex) {
            log.warn("Failed to clean up replaced KYC documents for user {}", userId, ex);
        }
    }

    @Override
    public List<Map<String, Object>> fetchAllKycs() {
        List<Users> users = userService.getAllUsers();
        if (users.isEmpty()) {
            throw new UserNotFoundException("No Such Users to show kyc");
        }
        return userService.getAllUsers()
                .stream()
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("user", userMapper.toDto(user));
                    response.put("kyc", user.getKycUrl() != null ? kycMapper.toResponse(user.getKycUrl()) : null);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public KycResponse updateKyc(
            String request,
            MultipartFile frontImage,
            MultipartFile backImage,
            MultipartFile selfie) {
        KycRequest kycData = readAndValidateKycData(request);
        Kyc savedKyc = kycRepository.findByUserId(kycData.getCustomerId())
                .orElseThrow(() -> new UserNotFoundException("User not found to retrive the kyc"));

        String newfrontImageUrl = fileService.uploadFile(frontImage);
        String newbackImageUrl = uploadOptionalFile(backImage);
        String newselfieUrl = fileService.uploadFile(selfie);

        Stream.of(
                savedKyc.getFrontImageUrl(),
                savedKyc.getBackImageUrl(),
                savedKyc.getSelfieUrl())
                .filter(path -> path != null && !path.isEmpty())
                .forEach(fileService::deleteFile);

        savedKyc.setFrontImageUrl(newfrontImageUrl);
        savedKyc.setBackImageUrl(newbackImageUrl);
        savedKyc.setSelfieUrl(newselfieUrl);
        Kyc response = kycRepository.save(kycMapper.updateEntity(savedKyc, kycData));
        return kycMapper.toResponse(response);
    }

    @Transactional
    @Override
    public void deleteKyc(Integer kycId) {
        log.info("kyc id {} to delete ", kycId);
        Kyc kyc = kycRepository.findById(kycId)
                .orElseThrow(() -> new KycFailedException("No Kyc for id: " + String.valueOf(kycId)));

        Users user = kyc.getUser();
        if (user != null) {
            user.setKycUrl(null);
        }
        List<Long> kycMetadataIds = imageMetadataRepository.getAllKycMetadatasIds(kyc.getUser().getId(),
                ImageMetadataTypes.KYC);
        kycMetadataIds.forEach(id -> supabaseFileStorageService.deleteFile(id, "private"));
        // Stream.of(
        // kyc.getFrontImageUrl(),
        // kyc.getBackImageUrl(),
        // kyc.getSelfieUrl())
        // .filter(path -> path != null && !path.isEmpty())
        // .forEach(path -> {
        // try {
        // fileService.deleteFile(path);
        // } catch (RuntimeException ex) {
        // log.warn("Failed to delete KYC file {} for kyc id {}", path, kycId, ex);
        // }
        // });
        kycRepository.delete(kyc);
    }

    // Helper methods to upload optional files and read/validate KYC data
    private String uploadOptionalFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return fileService.uploadFile(file);
    }

    // Helper methods to read and validate KYC data from JSON string
    private KycRequest readAndValidateKycData(String kycDataJson) {
        try {
            KycRequest kycData = objectMapper.readValue(kycDataJson, KycRequest.class);
            Set<ConstraintViolation<KycRequest>> violations = validator.validate(kycData);
            if (!violations.isEmpty()) {
                String message = violations.stream()
                        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                        .collect(Collectors.joining(", "));
                throw new IllegalArgumentException(message);
            }
            return kycData;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("kycData must be valid JSON");
        }
    }

    @Override
    public KycResponse updateKycStatus(Integer kycId, String status) {
        Kyc kyc = kycRepository.findById(kycId)
                .orElseThrow(() -> new KycFailedException("No Kyc for id: " + String.valueOf(kycId)));
        KycStatus kycStatus = Stream.of(KycStatus.values())
                .filter(s -> s.name().equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid KYC status: " + status));
        kyc.setStatus(kycStatus);
        Kyc savedKyc = kycRepository.save(kyc);

        // Push: notify the KYC owner about the admin's decision. Sent only after
        // the status change has been persisted; delivery is best-effort inside
        // NotificationService and never rolls the update back.
        Users owner = savedKyc.getUser();
        if (kycStatus == KycStatus.APPROVED) {
            notificationService.sendToUser(owner, "KYC Verified",
                    "Your KYC has been successfully verified.",
                    NotificationType.KYC_APPROVED, String.valueOf(savedKyc.getId()));
        } else if (kycStatus == KycStatus.REJECTED) {
            notificationService.sendToUser(owner, "KYC Verification Rejected",
                    "Your KYC verification was rejected. Please review the reason and reapply.",
                    NotificationType.KYC_REJECTED, String.valueOf(savedKyc.getId()));
        }

        return kycMapper.toResponse(savedKyc);
    }

    @Override
    public KycResponse getMyKyc(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotFoundException("User is not authenticated");
        }
        Users user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));
        Kyc kyc = kycRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UserNotFoundException("KYC has not been submitted"));
        return kycMapper.toResponse(kyc);
    }
}
