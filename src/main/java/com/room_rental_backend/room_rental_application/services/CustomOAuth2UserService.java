package com.room_rental_backend.room_rental_application.services;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.room_rental_backend.room_rental_application.components.OAuth2UserInfoFactory;
import com.room_rental_backend.room_rental_application.enums.Roles;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.models.base_entity.OAuth2UserInfo;
import com.room_rental_backend.room_rental_application.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        log.info("============= loading user ============");
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        log.info("{}", provider);

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(provider, oAuth2User.getAttributes());

        if (userInfo.getEmail() == null || userInfo.getEmail().isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found for " + provider + "provider");
        }

        Users user = userRepository.findByEmail(userInfo.getEmail())
                .map(exisistingUser -> updateExistingUser(exisistingUser, userInfo))
                .orElseGet(() -> registerNewUser(provider, userInfo));

        return new DefaultOAuth2User(
                user.getAuthorities(),
                oAuth2User.getAttributes(),
                "email");
    }

    private Users registerNewUser(String provider, OAuth2UserInfo userInfo) {

Users user = Users.builder()
                 .email(userInfo.getEmail())
                 .profilePictureUrl(userInfo.getImageUrl())
                 .provider(provider)
                 .providerId(userInfo.getId())
                 .verified(false)
                 .isActive(true)
                 .roles(com.room_rental_backend.room_rental_application.enums.Roles.ROLE_USER)
                 .build();

        return userRepository.save(user);
    }

    private Users updateExistingUser(Users user, OAuth2UserInfo userInfo) {
        user.setProfilePictureUrl(userInfo.getImageUrl());
        return userRepository.save(user);
    }
}