package com.PorTracker.PorTrackerBE.global.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import javax.annotation.PostConstruct;

@Component
public class JwtUtils {

    @Value("${supabase.jwt-secret}")
    private String jwtSecret;

    private SecretKey cachedKey;

    @PostConstruct
    public void init(){
        // 열쇠 객체 만들어두기
        this.cachedKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUserId(String token) {
        return getClaims(token).getSubject(); // JWT의 'sub' 필드가 유저의 UUID입니다.
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        
        return Jwts.parserBuilder()
                .setSigningKey(cachedKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}