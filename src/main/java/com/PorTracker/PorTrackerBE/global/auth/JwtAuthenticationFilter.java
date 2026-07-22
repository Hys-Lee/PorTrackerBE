package com.PorTracker.PorTrackerBE.global.auth;

import com.PorTracker.PorTrackerBE.global.common.UserContextHolder;
import com.PorTracker.PorTrackerBE.global.service.SyncManager;
import com.PorTracker.PorTrackerBE.global.service.UserInitializationService;
import io.jsonwebtoken.lang.Collections;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final SyncManager syncManager;
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-resource");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = parseBearerToken(request);

        if (token != null && jwtUtils.validateToken(token)) {
            String userId = jwtUtils.extractUserId(token);

            // 다중 기기 중복 접속 제어 (Single Active Session)
            if (!validateActiveSession(userId, token, response)) {
                return; // 중복 접속 차단 시 필터 체인 즉시 종료
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 핵심: 검증된 진짜 ID를 컨텍스트에 저장
            UserContextHolder.setUserId(userId);

            // 활동 시간 갱신 및 LRU 유저 제어
            syncManager.registerOrUpdateAccess(userId);

            userInitializationService.initializeUserDatabase(userId);

            log.info("Authentication user: {}, SecurityContext updated", userId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 요청이 끝나면 ThreadLocal 청소 (메모리 누수 방지)
            UserContextHolder.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private boolean validateActiveSession(String userId, String token, HttpServletResponse response) throws IOException {
        String sessionKey = "active_session:" + userId;
        String activeToken = redisTemplate.opsForValue().get(sessionKey);

        if (activeToken == null) {
            // 최초 접속 혹은 만료 시 현재 세션을 등록
            redisTemplate.opsForValue().set(sessionKey, token, 7, TimeUnit.DAYS);
            return true;
        }

        // 기존 기기와 동일한 토큰인 경우 통과
        if (activeToken.equals(token)) {
            return true;
        }

        // 다른 기기의 신규/과거 로그인 분별을 위해 토큰 발급 시간(issuedAt) 비교
        try {
            java.util.Date currentIat = jwtUtils.extractIssuedAt(token);
            java.util.Date activeIat = jwtUtils.extractIssuedAt(activeToken);

            if (currentIat != null && activeIat != null && currentIat.after(activeIat)) {
                // 들어온 토큰이 더 최근에 발급된 신규 로그인 기기 토큰일 경우
                String lockKey = "lock:session:" + userId;
                RLock lock = redissonClient.getLock(lockKey);
                try {
                    if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                        String recheckedToken = redisTemplate.opsForValue().get(sessionKey);
                        if (recheckedToken == null || jwtUtils.extractIssuedAt(token).after(jwtUtils.extractIssuedAt(recheckedToken))) {
                            redisTemplate.opsForValue().set(sessionKey, token, 7, TimeUnit.DAYS);
                            log.info("[ActiveSession] User {} registered a newer session token from another device", userId);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Session lock acquisition interrupted");
                    return false;
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
                return true;
            } else {
                // 들어온 토큰이 기존 활성화된 다른 기기 세션보다 과거인 경우 -> 차단
                log.warn("[ActiveSession] Multiple device login detected for user: {}. Restricting access...", userId);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Multiple device login detected. This session has been terminated.");
                return false;
            }
        } catch (Exception e) {
            log.error("[ActiveSession] Error validating active session for user: {}", userId, e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid active session metadata.");
            return false;
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
