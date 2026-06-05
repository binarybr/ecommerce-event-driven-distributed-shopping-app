package com.binarylabyrinth.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JwtUtil - issues and validates HS-signed JWTs.
 *
 * This is the ONLY service that MINTS tokens (generateToken). Every other
 * service holds a copy of JwtUtil that only VALIDATES — they all share the
 * same {@code jwt.secret} (symmetric HMAC), so a token signed here verifies
 * everywhere. The token's subject is the email; userId and role travel as
 * claims so downstream services authorize locally without a callback.
 *
 * SECURITY NOTE: the default secret is a dev convenience. In production set
 * {@code JWT_SECRET} to a long, random value via environment/secret manager.
 * A symmetric secret means any service could in principle mint tokens — for a
 * stronger boundary, switch to RS256 (private key here, public keys elsewhere).
 */
@Component
public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret:BinaryLabyrinthOnlineShoppingAppSecretKeyForJWTTokenSigningPurpose}")
    private String jwtSecret;

    /** Token lifetime in ms (default 24h). */
    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    /** Derive the HMAC signing key from the shared secret bytes. */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /** Mint a signed token: subject=email, plus userId and role claims. */
    public String generateToken(String email, Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return getClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationTime() {
        return jwtExpirationMs;
    }
}
