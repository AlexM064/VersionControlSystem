package com.sap.vcs.server.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private static final String ROLES_CLAIM = "roles";
    private static final String ENABLED_CLAIM = "enabled";

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expiration;

    public String generateToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.startsWith("ROLE_")
                        ? authority.substring(5)
                        : authority)
                .toList();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(ROLES_CLAIM, roles)
                .claim(ENABLED_CLAIM, userDetails.isEnabled())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        Claims claims = getClaims(token);
        Object rolesClaim = claims.get(ROLES_CLAIM);

        if (rolesClaim == null) {
            return List.of();
        }

        if (rolesClaim instanceof Collection<?> collection) {
            return collection.stream()
                    .map(String::valueOf)
                    .toList();
        }

        throw new IllegalArgumentException("Invalid roles claim in JWT");
    }

    public boolean extractEnabled(String token) {
        Claims claims = getClaims(token);
        Boolean enabled = claims.get(ENABLED_CLAIM, Boolean.class);
        return Boolean.TRUE.equals(enabled);
    }

    public boolean isValid(String token) {
        return !isTokenExpired(token);
    }

    public boolean isValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername())
                && userDetails.isEnabled()
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}