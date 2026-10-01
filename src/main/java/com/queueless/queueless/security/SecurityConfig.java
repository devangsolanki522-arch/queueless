package com.queueless.queueless.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // =========================
                        // PUBLIC ENDPOINTS
                        // =========================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/login"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/users"
                        ).permitAll()

                        .requestMatchers(
                                "/error"
                        ).permitAll()


                        // =========================
                        // SERVICE ENDPOINTS
                        // =========================

                        // Customers can view available services.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/services"
                        ).hasRole("CUSTOMER")

                        // Staff can view their own service.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/services/my"
                        ).hasRole("STAFF")

                        // Only staff can create services.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/services"
                        ).hasRole("STAFF")


                        // =========================
                        // CUSTOMER QUEUE ACTIONS
                        // =========================

                        // Customer joins queue.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/tokens"
                        ).hasRole("CUSTOMER")

                        // Customer cancels their token.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/tokens/*/cancel"
                        ).hasRole("CUSTOMER")

                        // Customer can check their queue position.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/tokens/*/queue"
                        ).hasAnyRole(
                                "CUSTOMER",
                                "STAFF"
                        )


                        // =========================
                        // STAFF QUEUE ACTIONS
                        // =========================

                        // Staff sees waiting customers.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/tokens/service/*/waiting"
                        ).hasRole("STAFF")

                        // Staff calls next customer.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/tokens/call-next/**"
                        ).hasRole("STAFF")

                        // Staff changes priority.
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/tokens/*/priority"
                        ).hasRole("STAFF")

                        // Staff marks customer as no-show.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/tokens/*/no-show"
                        ).hasRole("STAFF")

                        // Staff starts serving.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/tokens/*/start"
                        ).hasRole("STAFF")

                        // Staff completes token.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/tokens/*/complete"
                        ).hasRole("STAFF")


                        // =========================
                        // AUTHENTICATED FALLBACK
                        // =========================

                        // Any endpoint not explicitly listed
                        // still requires a valid JWT.
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}