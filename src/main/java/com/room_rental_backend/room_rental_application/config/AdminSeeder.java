package com.room_rental_backend.room_rental_application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.room_rental_backend.room_rental_application.enums.Roles;
import com.room_rental_backend.room_rental_application.models.Users;
import com.room_rental_backend.room_rental_application.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// New API/bootstrap: seeds a single ROLE_ADMIN account on startup. Idempotent
// (skips when the email already exists) and password-encoded, so it is safe to
// run on every boot. Credentials are overridable via APP_ADMIN_EMAIL /
// APP_ADMIN_PASSWORD env vars; the defaults are for local development only.
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@roomrental.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@123}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        String email = adminEmail == null ? null : adminEmail.trim().toLowerCase();
        if (email == null || email.isEmpty()) {
            return;
        }
        if (userRepository.existsByEmail(email)) {
            log.info("Admin account already present for {}, skipping seed", email);
            return;
        }

        Users admin = Users.builder()
                .email(email)
                .password(passwordEncoder.encode(adminPassword))
                .roles(Roles.ROLE_ADMIN)
                .fname("System")
                .lname("Admin")
                .build();

        userRepository.save(admin);
        log.info("Seeded default admin account for {}. Change the password after first login.", email);
    }
}
