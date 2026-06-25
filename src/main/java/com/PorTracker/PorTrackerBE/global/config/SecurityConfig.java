package com.PorTracker.PorTrackerBE.global.config;

import com.PorTracker.PorTrackerBE.global.auth.JwtAuthenticationFilter;
import com.PorTracker.PorTrackerBE.global.auth.RateLimitingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // API 서버이므로 CSRF 비활성화
                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS)) // 세션 미사용
                .authorizeHttpRequests(
                        auth ->
                                auth
                                        // 스웨거 관련
                                        .requestMatchers(
                                                "/swagger-ui/**",
                                                "/v3/api-docs/**",
                                                "/swagger-resource/**",
                                                "/webjars/**")
                                        .permitAll()
                                        .requestMatchers("/api/v1/public/**")
                                        .permitAll() // 공개 API
                                        .anyRequest()
                                        .authenticated() // 나머지는 모두 인증 필요
                        )
                // 우리가 만든 JWT 필터를 시큐리티 필터 체인에 등록
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // JWT 필터 직후에 Rate Limiting 필터 작동 연계
                .addFilterAfter(
                        rateLimitingFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
