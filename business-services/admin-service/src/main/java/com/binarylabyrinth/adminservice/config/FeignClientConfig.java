package com.binarylabyrinth.adminservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Forwards the inbound Authorization header onto outbound Feign calls so
 * downstream services see the same ADMIN JWT and pass their own
 * @PreAuthorize checks.
 *
 * Without this:
 *   - user-service GET /api/users (ADMIN-only) → 403
 *   - order-service PUT .../status (ADMIN-only) → 403
 */
@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor authorizationHeaderForwarder() {
        return template -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return;

            HttpServletRequest req = attrs.getRequest();
            String authHeader = req.getHeader("Authorization");
            if (authHeader != null && !authHeader.isBlank()) {
                template.header("Authorization", authHeader);
            }
        };
    }
}
