package com.binarylabyrinth.reviewservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = extractToken(request);
            if (StringUtils.hasText(jwt) && jwtUtil.isTokenValid(jwt)) {
                String email = jwtUtil.getEmailFromToken(jwt);
                String role  = jwtUtil.getRoleFromToken(jwt);
                Long userId  = jwtUtil.getUserIdFromToken(jwt);

                var authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role));
                // Pass userId as the credentials slot — controllers can grab it
                // from Authentication.getCredentials() to avoid re-parsing the JWT.
                var auth = new UsernamePasswordAuthenticationToken(email, userId, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            log.error("JWT processing failed", e);
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
