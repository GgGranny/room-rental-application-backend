package com.room_rental_backend.room_rental_application.models;

import java.time.Instant;

import com.room_rental_backend.room_rental_application.models.base_entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "activation_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivationToken extends BaseEntity {

    private String token;

    private Instant expirationTime;

    @OneToOne
    @JoinColumn(name = "user_id")
    private Users user;

}
