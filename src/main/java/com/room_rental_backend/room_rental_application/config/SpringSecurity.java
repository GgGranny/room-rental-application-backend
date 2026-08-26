package com.room_rental_backend.room_rental_application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.room_rental_backend.room_rental_application.components.CustomAccessDenied;
import com.room_rental_backend.room_rental_application.components.CustomAuthEntryPoint;
import com.room_rental_backend.room_rental_application.components.JwtAuthFilter;
import com.room_rental_backend.room_rental_application.components.OAuth2FailureHandler;
import com.room_rental_backend.room_rental_application.components.OAuth2SuccessHandler;
import com.room_rental_backend.room_rental_application.services.CustomOAuth2UserService;
import com.room_rental_backend.room_rental_application.services.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SpringSecurity {

    private final JwtAuthFilter jwtAuthFilter;

    private final CustomUserDetailsService userDetailsService;

    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    private final OAuth2FailureHandler oAuth2FailureHandler;

    private final CustomOAuth2UserService customOAuth2UserService;

    private final CustomAuthEntryPoint customAuthEntryPoint;

    private final CustomAccessDenied customAccessDenied;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/rooms/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/rooms/**").hasRole("LANDLORD")

                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/login/oauth2/**").permitAll()

                        .requestMatchers("/api/v1/user/**").hasRole("USER")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/property", "/api/v1/property/**").hasRole("LANDLORD")
                        .requestMatchers("/api/v1/landlord/**").hasRole("LANDLORD")
                        .requestMatchers("/api/v1/kyc/**").hasAnyRole("USER", "LANDLORD")
                        .requestMatchers("/api/v1/roommates/**").hasRole("USER")
                        .anyRequest().authenticated())

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .formLogin(Customizer.withDefaults())

                .httpBasic(AbstractHttpConfigurer::disable)

                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint
                                .baseUri("/oauth2/authorize"))
                        .redirectionEndpoint(endpoint -> endpoint.baseUri("/login/oauth2/code/*"))
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler))

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .authenticationProvider(authenticationProvider())

                .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                .exceptionHandling(exception -> exception.authenticationEntryPoint(customAuthEntryPoint)
                        .accessDeniedHandler(customAccessDenied))
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
