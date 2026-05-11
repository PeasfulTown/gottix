package xyz.peasfultown.gottix.auth_service.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import xyz.peasfultown.gottix.auth_service.entity.UserEntity;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {
    private final long expiry;
    private final SecretKey jwtSigningKey;

    @Autowired
    public JwtUtil(@Value("${jwt.expiration-ms}") long expiry, SecretKey jwtSigningKey) {
        this.expiry = expiry;
        this.jwtSigningKey = jwtSigningKey;
    }

    public String generateAccessToken(UserEntity user) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().getAuthority())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiry))
                .signWith(jwtSigningKey)
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtSigningKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims;
    }
}
