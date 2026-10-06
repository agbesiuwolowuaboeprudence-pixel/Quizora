package com.quizora.backend.security;

import com.quizora.backend.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    /** Minimum HS256 key length enforced by jjwt; validated up-front with a clear message. */
    public static final int MIN_SECRET_BYTES = 32;

    public JwtService(@Value("${quizora.security.jwt.secret}") String secret,
                      @Value("${quizora.security.jwt.expiration-ms}") long expirationMs,
                      Environment environment) {
        boolean demo = Arrays.stream(environment.getActiveProfiles()).anyMatch("demo"::equalsIgnoreCase);
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length == 0 && demo) {
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        } else if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT secret is too weak: at least 32 characters are required for HS256 "
                            + "(got " + secretBytes.length + "). Set the JWT_SECRET environment variable.");
        } else {
            this.key = Keys.hmacShaKeyFor(secretBytes);
        }
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    /** Returns the claims when the token is valid, or null when it is missing/expired/tampered. */
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}
