package com.HospitalManagement.shared.security;

import com.HospitalManagement.shared.enums.Roles;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> {
            Object userId = claims.get("userId");
            if (userId instanceof Number number) {
                return number.longValue();
            }
            return null;
        });
    }

    public String extractName(String token) {
        return extractClaim(token, claims -> claims.get("name", String.class));
    }

    public Roles extractRole(String token) {
        return extractClaim(token, claims -> {
            String roleStr = claims.get("role", String.class);
            if (roleStr != null) {
                try {
                    return Roles.valueOf(roleStr);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
            return null;
        });
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token) {
        try {
            return !extractAllClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
