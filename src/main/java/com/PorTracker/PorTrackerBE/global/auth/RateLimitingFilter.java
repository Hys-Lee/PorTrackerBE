package com.PorTracker.PorTrackerBE.global.auth;

import com.PorTracker.PorTrackerBE.global.common.UserContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

    // 유저당 분당 최대 API 허용 요청 횟수
    private static final int MAX_REQUESTS_PER_MINUTE = 100;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        // 스웨거 및 공개 API는 유량 제어 필터 제외
        if (path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-resource")
                || path.startsWith("/api/v1/public")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 로그인된 유저 ID 획득 (JWT 필터 이후에 오므로 Context에 세팅됨)
        String userId = UserContextHolder.getUserId();
        String limitKey =
                (userId != null)
                        ? "ratelimit:user:" + userId
                        : "ratelimit:ip:" + request.getRemoteAddr();

        // 1분(60초) 단위 윈도우 시간 타임스탬프 계산
        long currentMinute = System.currentTimeMillis() / 60000;
        String redisKey = limitKey + ":" + currentMinute;

        try {
            // Redis 카운터 증폭
            Long currentCount = redisTemplate.opsForValue().increment(redisKey);

            if (currentCount != null && currentCount == 1) {
                // 최초 1회 생성 시 60초 TTL 설정
                redisTemplate.expire(redisKey, Duration.ofSeconds(60));
            }

            if (currentCount != null && currentCount > MAX_REQUESTS_PER_MINUTE) {
                log.warn(
                        "[RateLimit] Limit exceeded for key: {}. Count: {}",
                        limitKey,
                        currentCount);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter()
                        .write(
                                "{\"error\": \"Too Many Requests\", \"message\": \"요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.\"}");
                return;
            }
        } catch (Exception e) {
            // Redis 순단 시에도 서비스는 지속되도록 예외 차단
            log.error("[RateLimit] Failed to compute rate limit in Redis", e);
        }

        filterChain.doFilter(request, response);
    }
}
