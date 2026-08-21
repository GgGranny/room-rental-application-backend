package com.room_rental_backend.room_rental_application.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    // Optional override for the service-account file location (project root by default).
    @Value("${app.firebase.credentials-path:firebase-service-account.json}")
    private String credentialsPath;

    @PostConstruct
    public void initialize() {
        // Guard against re-initialisation if the app is recreated by the container.
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try (InputStream serviceAccount = resolveCredentialsStream()) {
            if (serviceAccount == null) {
                log.warn("Firebase service-account file '{}' was not found. Push notifications will be disabled.", credentialsPath);
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK initialized for project {}", FirebaseApp.getInstance().getOptions().getProjectId());
        } catch (Exception e) {
            // Safe failure: log without exposing credential contents and let the
            // rest of the application keep running (push becomes unavailable).
            log.warn("Firebase initialization failed. Push notifications will be disabled: {}", e.getMessage());
        }
    }

    private InputStream resolveCredentialsStream() throws Exception {
        File file = new File(credentialsPath);
        if (file.exists() && file.isFile()) {
            return new FileInputStream(file);
        }
        // Fall back to the classpath so the app still works when packaged as a jar.
        ClassPathResource resource = new ClassPathResource(credentialsPath);
        if (resource.exists()) {
            return resource.getInputStream();
        }
        return null;
    }
}