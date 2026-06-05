package com.binarylabyrinth.inventoryservice.config;

import com.binarylabyrinth.inventoryservice.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Inventory authorization:
 *   - POST /reserve  : ADMIN or CUSTOMER (called by order-service via Feign
 *                      with the placing customer's forwarded JWT)
 *   - POST /          : ADMIN only (stock replenishment)
 *   - GET  /all       : ADMIN only (dashboard listing)
 *   - GET  /          : ADMIN or CUSTOMER (read-only availability check)
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/inventory/reserve").hasAnyRole("ADMIN", "CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/inventory").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/inventory/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/inventory").hasAnyRole("ADMIN", "CUSTOMER")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
