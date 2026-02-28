package com.PorTracker.PorTrackerBE.global.auth;

import com.PorTracker.PorTrackerBE.global.common.UserContextHolder;
import com.PorTracker.PorTrackerBE.global.service.UserInitializationService;

import io.jsonwebtoken.lang.Collections;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    private final UserInitializationService userInitializationService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = parseBearerToken(request);

        if (token != null && jwtUtils.validateToken(token)) {
            String userId = jwtUtils.extractUserId(token);
            
            //
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
            
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 핵심: 검증된 진짜 ID를 컨텍스트에 저장
            UserContextHolder.setUserId(userId);

            userInitializationService.initializeUserDatabase(userId);

            log.info("Authentication user: {}, SecurityContext updated",userId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 요청이 끝나면 ThreadLocal 청소 (메모리 누수 방지)
            UserContextHolder.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private String parseBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // test
        log.info("원본 토큰은 먼데: {}", bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
