package com.room_rental_backend.room_rental_application.models;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.room_rental_backend.room_rental_application.enums.Roles;
import com.room_rental_backend.room_rental_application.models.base_entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Users extends BaseEntity implements UserDetails {

    @Column(name = "password")
    private String password; // nullable true, to for OAuth auth

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "phone_number", unique = true, length = 10)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Roles roles;

    @Column(name = "first_name", length = 50)
    private String fname;

    @Column(name = "last_name", length = 50)
    private String lname;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Kyc kycUrl;

    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "provider_id")
    private String providerId;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private RefreshToken refreshToken;

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private boolean verified = true;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true; // for develepment purpose only true

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Landlord landlord;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private ActivationToken activationToken;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null) {
            return Collections.emptyList();
        }
        return List.of(new SimpleGrantedAuthority(roles.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

}
