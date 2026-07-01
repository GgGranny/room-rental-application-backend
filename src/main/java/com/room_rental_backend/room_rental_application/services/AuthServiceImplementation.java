package com.room_rental_backend.room_rental_application.services;

import java.security.Principal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;

import com.room_rental_backend.room_rental_application.components.HttpCookieComponent;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.room_rental_backend.room_rental_application.components.CustomAuthEntryPoint;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.CompleteUserProfileRequest;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.RefreshTokenRequest;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.RegisterUserRequestDtos;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.UserLoginRequestDto;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.AuthResponse;
import com.room_rental_backend.room_rental_application.enums.Roles;
import com.room_rental_backend.room_rental_application.events.UserRegisterEvent;
import com.room_rental_backend.room_rental_application.exceptions.EmailAlreadyExistsException;
import com.room_rental_backend.room_rental_application.exceptions.PhoneNumberAlreadyExists;
import com.room_rental_backend.room_rental_application.exceptions.TokenExpiredException;
import com.room_rental_backend.room_rental_application.exceptions.TokenNotFoundException;
import com.room_rental_backend.room_rental_application.exceptions.UnauthorizedException;
import com.room_rental_backend.room_rental_application.exceptions.UserNotFoundException;
import com.room_rental_backend.room_rental_application.interfaces.AuthService;
import com.room_rental_backend.room_rental_application.interfaces.RefreshTokenService;
import com.room_rental_backend.room_rental_application.mappers.AuthMapper;
import com.room_rental_backend.room_rental_application.models.ActivationToken;
import com.room_rental_backend.room_rental_application.models.Landlord;
import com.room_rental_backend.room_rental_application.models.RefreshToken;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.repositories.ActivationTokenRepository;
import com.room_rental_backend.room_rental_application.repositories.LandlordRepository;
import com.room_rental_backend.room_rental_application.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImplementation implements AuthService {

    private final UserRepository userRepository;

    private final ActivationTokenRepository activationTokenRepository;

    private final AuthenticationManager authenticationManager;

    private final AuthMapper authMapper;

    private final JwtService jwtService;

    private final ApplicationEventPublisher eventPublisher;

    private final RefreshTokenService refreshTokenService;

    private final HttpCookieComponent httpCookieComponent;

    private final LandlordRepository landlordRepository;

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImplementation.class);

    @Override
    public AuthResponse register(RegisterUserRequestDtos request) {

        String email = request.email().trim().toLowerCase();
        // String phoneNumber = request.phoneNumber().trim();
        // if (userRepository.existsByPhoneNumber(phoneNumber)) {
        // throw new PhoneNumberAlreadyExists("Phone number already exists");
        // }
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        Users newUser = authMapper.toUsers(request);
        Users savedUser = userRepository.save(newUser);

        // Generate OTP and publish the event to event listener
        String otp = generateOpt();
        ActivationToken activationToken = new ActivationToken();
        activationToken.setToken(otp);
        activationToken.setExpirationTime(Instant.now().plusSeconds(60 * 60 * 24));
        activationToken.setUser(savedUser);
        activationTokenRepository.save(activationToken);

        String activationUrl = "http://localhost:8000/api/v1/auth/activate?token=" + otp;
        // publisUserRegisterEvent(savedUser, activationUrl);
        return authMapper.toAuthResponse(savedUser, null, null);
    }

    @Override
    public AuthResponse login(UserLoginRequestDto request) {
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        Users user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("No User For For " + request.email() + "Email"));

        String jwtToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return authMapper.toAuthResponse(user, jwtToken, refreshToken.getToken());
    }

    // Publishe the userRegistred Event
    private void publisUserRegisterEvent(Users user, String activationUrl) {
        eventPublisher.publishEvent(new UserRegisterEvent(user.getEmail(), activationUrl));
    }

    // Generates the 6 digit otp
    private String generateOpt() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Override
    public AuthResponse activateUser(String token) {
        ActivationToken savedToken = activationTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException(token + " Activation token not found"));

        Users notActivatedUser = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException("User not found for id: " + savedToken.getUser().getId()));

        if (savedToken.getExpirationTime().isBefore(Instant.now())) {
            activationTokenRepository.delete(savedToken);
            throw new TokenExpiredException("Token is alreday exipred");
        }

        try {
            notActivatedUser.setActive(true);
            Users activatedUser = userRepository.save(notActivatedUser);
            // delete the token after the activation
            activationTokenRepository.delete(savedToken);

            // String jwtToken = jwtService.generateToken(activatedUser);
            // save the refresh token to db
            // RefreshToken refreshToken =
            // refreshTokenService.saveRefreshToken(activatedUser);
            return authMapper.toAuthResponse(null, null, null);
        } catch (Exception e) {
            logger.warn("{}", e.getMessage());
            return null;
        }
    }

    // @Override
    // public AuthResponse refreshToken(RefreshTokenRequest refreshToken) {
    // String refreshTokenReq = refreshToken.refreshToken();
    // RefreshToken token = refreshTokenService.validateToken(refreshTokenReq);
    //
    // String userId = token.getUser().getId();
    // Users user = userRepository.findById(userId)
    // .orElseThrow(
    // () -> new UserNotFoundException("User of id: " + userId + " Does not
    // exists"));
    // String newJwtToken = jwtService.generateToken(user);
    //
    // return authMapper.toAuthResponse(user, newJwtToken, refreshTokenReq);
    //
    // }
    @Override
    public AuthResponse refreshToken(String refreshToken) {
        String refreshTokenReq = refreshToken;
        RefreshToken token = refreshTokenService.validateToken(refreshTokenReq);

        String userId = token.getUser().getId();
        Users user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new UserNotFoundException("User of id: " + userId + " Does not exists"));
        String newJwtToken = jwtService.generateToken(user);

        return authMapper.toAuthResponse(user, newJwtToken, refreshTokenReq);

    }

    @Override
    public AuthResponse completeUserProfile(CompleteUserProfileRequest request) {

        Users user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found for id: " + request.userId()));

        // Update fields
        user.setRoles(request.role());
        user.setFname(request.fname());
        user.setLname(request.lname());
        user.setDateOfBirth(request.dob());

        if (request.role() == Roles.ROLE_LANDLORD) {
            Landlord landlord = new Landlord();
            landlord.setUser(user);
            user.setLandlord(landlord);
        }

        Users savedUser = userRepository.save(user);

        // Generate new tokens with role
        String jwtToken = jwtService.generateToken(savedUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        return authMapper.toAuthResponse(savedUser, jwtToken, refreshToken.getToken());
    }

    // check if user profile setup is complete
    // @Override
    // public boolean isProfileCompleted() {
    //// Authentication auth =
    // SecurityContextHolder.getContext().getAuthentication();
    //// if(auth == null && !(auth.getPrincipal() instanceof UserDetails)) {
    //// throw new UserNotFoundException("User Does not exists");
    //// }
    //// String email = ((UserDetails) auth.getPrincipal()).getUsername();
    //// System.out.println("email: " + email);
    //// Users user = userRepository.findByEmail(email)
    //// .orElseThrow(()-> new UserNotFoundException("User does not exists"));
    //// return user.getRoles() != null
    //// && hasText(user.getFname())
    //// && hasText(user.getLname())
    //// && user.getDateOfBirth() != null;
    // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //
    // if (auth == null || auth.getPrincipal() == null) {
    // throw new UserNotFoundException("User Does not exist");
    // }
    //
    // Object principal = auth.getPrincipal();
    //
    // String email;
    //
    // if (principal instanceof UserDetails userDetails) {
    // email = userDetails.getUsername();
    // } else if (principal instanceof OAuth2User oauth2User) {
    // email = oauth2User.getAttribute("email");
    // } else {
    // throw new UserNotFoundException("Unsupported principal type");
    // }
    //
    // Users user = userRepository.findByEmail(email)
    // .orElseThrow(() ->
    // new UserNotFoundException("User does not exist"));
    // return user.getRoles() != null
    // && hasText(user.getFname())
    // && hasText(user.getLname())
    // && user.getDateOfBirth() != null;
    // }
    @Override
    public boolean isProfileCompleted() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UserNotFoundException("User is not authenticated");
        }

        Object principal = auth.getPrincipal();
        String email;

        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else if (principal instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
            if (email == null) {
                throw new UserNotFoundException("Email attribute missing from OAuth2 user");
            }
        } else {
            throw new UserNotFoundException("Unsupported principal type: "
                    + principal.getClass().getName());
        }

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "No account found for email: " + email));
        System.out.println(user.toString());
        System.out.println("roles: " + user.getRoles());
        System.out.println("fname: " + user.getFname());
        System.out.println("lname: " + user.getLname());
        System.out.println("dob: " + user.getDateOfBirth());
        return user.getRoles() != null
                && hasText(user.getFname())
                && hasText(user.getLname())
                && user.getDateOfBirth() != null;
    }

    @Override
    public AuthResponse getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotFoundException("not Authenticated");
        }
        String email = authentication.getName();

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user with email: " + email + " does not exists"));

        Landlord landlord = landlordRepository.findByUser(user);
        return AuthResponse.builder()
                .Dob(user.getDateOfBirth())
                .fname(user.getFname())
                .lname(user.getLname())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRoles())
                .isVerifird(user.isVerified())
                .refreshToken(null)
                .landlordId(user.getRoles().equals(Roles.ROLE_LANDLORD) ? landlord.getId() : null)
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

}
