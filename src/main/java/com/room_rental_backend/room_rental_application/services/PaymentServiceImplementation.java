package com.room_rental_backend.room_rental_application.services;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.PaymentInitiateRequest;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.PaymentVerifyRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.PaymentInitiateResponse;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.PaymentResponseDto;
import com.room_rental_backend.room_rental_application.enums.NotificationType;
import com.room_rental_backend.room_rental_application.enums.PaymentGateway;
import com.room_rental_backend.room_rental_application.enums.PaymentStatus;
import com.room_rental_backend.room_rental_application.exceptions.UnauthorizedException;
import com.room_rental_backend.room_rental_application.exceptions.UserNotFoundException;
import com.room_rental_backend.room_rental_application.interfaces.NotificationService;
import com.room_rental_backend.room_rental_application.interfaces.PaymentService;
import com.room_rental_backend.room_rental_application.mappers.PaymentMapper;
import com.room_rental_backend.room_rental_application.models.Landlord;
import com.room_rental_backend.room_rental_application.models.Payment;
import com.room_rental_backend.room_rental_application.models.Property;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.repositories.PaymentRepository;
import com.room_rental_backend.room_rental_application.repositories.PropertyRepository;
import com.room_rental_backend.room_rental_application.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// New API: featured-listing payments via eSewa (v2) and Khalti sandbox gateways.
// A landlord pays a fixed amount to feature one of their own properties for a
// fixed window. We persist a Payment (INITIATED) at initiate time and only flip
// it to SUCCESS + feature the property once the gateway confirms the transaction.
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImplementation implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;
    private final RestTemplate restTemplate;
    private final NotificationService notificationService;

    @Value("${app.payment.featured.amount}")
    private BigDecimal featuredAmount;
    @Value("${app.payment.featured.days}")
    private int featuredDays;
    @Value("${app.payment.frontend.success-url}")
    private String frontendSuccessUrl;
    @Value("${app.payment.frontend.failure-url}")
    private String frontendFailureUrl;

    @Value("${app.payment.esewa.merchant-code}")
    private String esewaMerchantCode;
    @Value("${app.payment.esewa.secret}")
    private String esewaSecret;
    @Value("${app.payment.esewa.form-url}")
    private String esewaFormUrl;
    @Value("${app.payment.esewa.status-url}")
    private String esewaStatusUrl;

    @Value("${app.payment.khalti.base-url}")
    private String khaltiBaseUrl;
    @Value("${app.payment.khalti.secret}")
    private String khaltiSecret;

    @Transactional
    @Override
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request, Authentication authentication) {
        Users user = resolveUser(authentication);
        Landlord landlord = requireLandlord(user);

        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new IllegalArgumentException("Property not found for id: " + request.propertyId()));

        // Only the owning landlord may pay to feature a property.
        if (property.getLandlord() == null || !property.getLandlord().getId().equals(landlord.getId())) {
            throw new UnauthorizedException("You can only feature your own properties");
        }

        Payment payment = Payment.builder()
                .landlord(landlord)
                .property(property)
                .amount(featuredAmount)
                .gateway(request.gateway())
                .status(PaymentStatus.INITIATED)
                .transactionUuid(UUID.randomUUID().toString())
                .featureDays(featuredDays)
                .build();
        paymentRepository.save(payment);

        return request.gateway() == PaymentGateway.ESEWA
                ? buildEsewaInitiation(payment)
                : buildKhaltiInitiation(payment);
    }

    // --- eSewa v2: signed hidden-field form the frontend auto-submits (POST) ---
    private PaymentInitiateResponse buildEsewaInitiation(Payment payment) {
        String totalAmount = formatAmount(payment.getAmount());
        String signature = esewaSignature(totalAmount, payment.getTransactionUuid(), esewaMerchantCode);

        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("amount", totalAmount);
        fields.put("tax_amount", "0");
        fields.put("total_amount", totalAmount);
        fields.put("transaction_uuid", payment.getTransactionUuid());
        fields.put("product_code", esewaMerchantCode);
        fields.put("product_service_charge", "0");
        fields.put("product_delivery_charge", "0");
        fields.put("success_url", frontendSuccessUrl);
        fields.put("failure_url", frontendFailureUrl);
        fields.put("signed_field_names", "total_amount,transaction_uuid,product_code");
        fields.put("signature", signature);

        return PaymentInitiateResponse.builder()
                .paymentId(payment.getId())
                .transactionUuid(payment.getTransactionUuid())
                .gateway(PaymentGateway.ESEWA)
                .amount(payment.getAmount())
                .redirectMethod("POST")
                .redirectUrl(esewaFormUrl)
                .formFields(fields)
                .build();
    }

    // --- Khalti: server-side initiate, browser then GETs the returned url ---
    @SuppressWarnings("unchecked")
    private PaymentInitiateResponse buildKhaltiInitiation(Payment payment) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("return_url", frontendSuccessUrl);
        body.put("website_url", frontendSuccessUrl);
        // Khalti expects the amount in paisa (NPR * 100).
        body.put("amount", payment.getAmount().multiply(BigDecimal.valueOf(100)).intValueExact());
        body.put("purchase_order_id", payment.getTransactionUuid());
        body.put("purchase_order_name", "Featured listing: " + payment.getProperty().getPropertyName());

        Map<String, Object> response;
        try {
            response = restTemplate.postForObject(
                    khaltiBaseUrl + "epayment/initiate/",
                    new HttpEntity<>(body, khaltiHeaders()),
                    Map.class);
        } catch (RestClientException ex) {
            log.warn("Khalti initiate failed for txn {}: {}", payment.getTransactionUuid(), ex.getMessage());
            throw new IllegalArgumentException("Could not start Khalti payment. Please try again.");
        }

        if (response == null || response.get("pidx") == null || response.get("payment_url") == null) {
            throw new IllegalArgumentException("Khalti did not return a payment session");
        }

        // Store the pidx as our gateway reference so verify can look this payment up.
        payment.setGatewayReference(String.valueOf(response.get("pidx")));
        paymentRepository.save(payment);

        return PaymentInitiateResponse.builder()
                .paymentId(payment.getId())
                .transactionUuid(payment.getTransactionUuid())
                .gateway(PaymentGateway.KHALTI)
                .amount(payment.getAmount())
                .redirectMethod("GET")
                .redirectUrl(String.valueOf(response.get("payment_url")))
                .formFields(null)
                .build();
    }

    @Transactional
    @Override
    public PaymentResponseDto verifyPayment(PaymentVerifyRequest request, Authentication authentication) {
        Users user = resolveUser(authentication);
        Landlord landlord = requireLandlord(user);

        Payment payment = resolvePayment(request);
        // A landlord may only verify their own payment.
        if (payment.getLandlord() == null || !payment.getLandlord().getId().equals(landlord.getId())) {
            throw new UnauthorizedException("You can only verify your own payments");
        }

        // Idempotent: already-confirmed payments are simply returned as-is.
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return paymentMapper.toResponse(payment);
        }

        boolean paid = payment.getGateway() == PaymentGateway.ESEWA
                ? verifyEsewa(payment)
                : verifyKhalti(payment);

        if (!paid) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            // Push: let the landlord know the payment did not go through.
            notificationService.sendToUser(user, "Payment Failed",
                    "Your featured-listing payment for \"" + payment.getProperty().getPropertyName()
                            + "\" failed. Please try again.",
                    NotificationType.PAYMENT_FAILED, payment.getId());
            throw new IllegalArgumentException("Payment was not completed at the gateway");
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        featureProperty(payment);

        // Push: confirm the featured-listing payment succeeded.
        notificationService.sendToUser(user, "Payment Successful",
                "Your featured-listing payment for \"" + payment.getProperty().getPropertyName()
                        + "\" was successful.",
                NotificationType.PAYMENT_SUCCESS, payment.getId());

        return paymentMapper.toResponse(payment);
    }

    // Turn on featuring; extend from an existing active window if still featured.
    private void featureProperty(Payment payment) {
        Property property = payment.getProperty();
        LocalDateTime now = LocalDateTime.now();
        // LocalDateTime base = property.isFeatured() && property.getFeaturedUntil() !=
        // null
        // && property.getFeaturedUntil().isAfter(now)
        // ? property.getFeaturedUntil()
        // : now;
        // property.setFeatured(true);
        // property.setFeaturedUntil(base.plusDays(payment.getFeatureDays()));
        propertyRepository.save(property);
    }

    @SuppressWarnings("unchecked")
    private boolean verifyEsewa(Payment payment) {
        String url = esewaStatusUrl + "?product_code=" + esewaMerchantCode
                + "&total_amount=" + formatAmount(payment.getAmount())
                + "&transaction_uuid=" + payment.getTransactionUuid();
        try {
            Map<String, Object> body = restTemplate.getForObject(url, Map.class);
            if (body == null) {
                return false;
            }
            if (body.get("ref_id") != null) {
                payment.setGatewayReference(String.valueOf(body.get("ref_id")));
            }
            return "COMPLETE".equalsIgnoreCase(String.valueOf(body.get("status")));
        } catch (RestClientException ex) {
            log.warn("eSewa status check failed for txn {}: {}", payment.getTransactionUuid(), ex.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean verifyKhalti(Payment payment) {
        if (payment.getGatewayReference() == null) {
            return false;
        }
        try {
            Map<String, Object> body = restTemplate.postForObject(
                    khaltiBaseUrl + "epayment/lookup/",
                    new HttpEntity<>(Map.of("pidx", payment.getGatewayReference()), khaltiHeaders()),
                    Map.class);
            if (body == null) {
                return false;
            }
            return "Completed".equalsIgnoreCase(String.valueOf(body.get("status")));
        } catch (RestClientException ex) {
            log.warn("Khalti lookup failed for txn {}: {}", payment.getTransactionUuid(), ex.getMessage());
            return false;
        }
    }

    @Override
    public List<PaymentResponseDto> getMyPayments(Authentication authentication) {
        Users user = resolveUser(authentication);
        Landlord landlord = requireLandlord(user);
        return paymentRepository.findByLandlordOrderByCreatedAtDesc(landlord)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    private Payment resolvePayment(PaymentVerifyRequest request) {
        if (request.transactionUuid() != null && !request.transactionUuid().isBlank()) {
            return paymentRepository.findByTransactionUuid(request.transactionUuid())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Payment not found for transaction: " + request.transactionUuid()));
        }
        if (request.pidx() != null && !request.pidx().isBlank()) {
            // Khalti callbacks may echo only the pidx we stored as the gateway reference.
            return paymentRepository.findByGatewayReference(request.pidx())
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found for pidx: " + request.pidx()));
        }
        throw new IllegalArgumentException("Provide either transactionUuid or pidx");
    }

    private Users resolveUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));
    }

    private Landlord requireLandlord(Users user) {
        Landlord landlord = user.getLandlord();
        if (landlord == null) {
            throw new UnauthorizedException("Only landlords can make featuring payments");
        }
        return landlord;
    }

    private HttpHeaders khaltiHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Key " + khaltiSecret);
        return headers;
    }

    // eSewa needs a plain decimal string (no scientific notation); the same string
    // must be used both in the signature and the submitted total_amount field.
    private String formatAmount(BigDecimal amount) {
        return amount.stripTrailingZeros().toPlainString();
    }

    private String esewaSignature(String totalAmount, String transactionUuid, String productCode) {
        String message = "total_amount=" + totalAmount
                + ",transaction_uuid=" + transactionUuid
                + ",product_code=" + productCode;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(esewaSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Could not sign the eSewa request", ex);
        }
    }
}
