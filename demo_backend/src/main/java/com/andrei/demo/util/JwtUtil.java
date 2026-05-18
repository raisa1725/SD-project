package com.andrei.demo.util;

import com.andrei.demo.model.Person;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    private Date getCurrentDate() {
        return Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC));
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(Person person) {
        return Jwts.builder()
                .subject(person.getEmail())
                .issuer("demo-spring-boot-backend")
                .issuedAt(getCurrentDate())
                .claim("userId", person.getId().toString())
                .claim("role", person.getRole().name())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean checkClaims(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);

            if (!"demo-spring-boot-backend".equals(claims.getIssuer())) {
                log.error("Invalid token issuer");
                return false;
            }

            if (claims.getExpiration().before(getCurrentDate())) {
                log.error("Token has expired");
                return false;
            }

            if (claims.getIssuedAt() == null || claims.getIssuedAt().after(getCurrentDate())) {
                log.error("Token issued at date is invalid");
                return false;
            }

            if (claims.get("userId") == null || claims.get("role") == null) {
                log.error("Token claims are invalid: missing userId or role");
                return false;
            }

            log.info("Token is valid. User ID: {}, Role: {}",
                    claims.get("userId"), claims.get("role"));

            return true;
        } catch (Exception exception) {
            log.error("Invalid token: {}", exception.getMessage());
            return false;
        }
    }

    public String extractEmail(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    public String extractRole(String token) {
        return getAllClaimsFromToken(token).get("role", String.class);
    }

    public String extractUserId(String token) {
        return getAllClaimsFromToken(token).get("userId", String.class);
    }
}