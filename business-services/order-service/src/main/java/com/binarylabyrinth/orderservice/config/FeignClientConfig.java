package com.binarylabyrinth.orderservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Forwards the inbound request's Authorization header onto outbound Feign
 * calls. Required because user-service's GET /api/users/{id} is gated by
 * @PreAuthorize — without this, Feign calls from this service get 403 and
 * the customerEmail field on OrderPlacedEvent ends up empty.
 *
 * RequestContextHolder pulls the current HTTP request's headers from the
 * thread-local context populated by Spring's RequestContextFilter (which
 * runs by default in spring-boot-starter-web).
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
