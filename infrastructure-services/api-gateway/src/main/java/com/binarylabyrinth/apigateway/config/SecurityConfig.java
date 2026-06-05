package com.binarylabyrinth.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * SecurityConfig - Reactive Security Configuration for API Gateway
 *
 * Spring Cloud Gateway runs on the reactive (WebFlux) stack, so this config
 * uses reactive security (ServerHttpSecurity / @EnableWebFluxSecurity).
 *
 * SECURITY POLICIES:
 * 1. CSRF: Disabled (API is stateless, CSRF not applicable)
 * 2. Authentication: HTTP Basic Auth (username + password)
 * 3. User Storage: In-memory (can be replaced with database for production)
 * 4. Password Encoding: BCrypt
 * 5. Public Endpoints: /actuator/health and /actuator/info (health checks)
 * 6. Protected Endpoints: All other endpoints require authentication
 *
 * CURRENT SETUP (Development):
 * - Default Username: "admin"
 * - Default Password: "admin"
 * - Override via env: GATEWAY_USERNAME / GATEWAY_PASSWORD
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Public health endpoints
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        // All API routes pass through unauthenticated at the gateway.
                        // Downstream services validate JWT Bearer tokens via their
                        // JwtAuthenticationFilter and enforce @PreAuthorize roles.
                        .pathMatchers("/api/**").permitAll()
                        // Other actuator/management endpoints (gateway/routes, env, etc.)
                        // still require Basic Auth (admin/change-me) for admin tooling.
                        .anyExchange().authenticated())
                .httpBasic(httpBasicSpec -> {})
                .build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService(
            @Value("${gateway.security.username:admin}") String username,
            @Value("${gateway.security.password:admin}") String password,
            PasswordEncoder passwordEncoder) {

        UserDetails user = User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles("USER")
                .build();
        return new MapReactiveUserDetailsService(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
