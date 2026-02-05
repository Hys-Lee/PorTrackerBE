package com.PorTracker.PorTrackerBE.global.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync // 비동기 기능
public class AsyncConfig {
    @Bean(name = "statsExecutor")
    public Executor statsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // 기본 유지 thread 수
        executor.setMaxPoolSize(10); // 최대 수
        executor.setQueueCapacity(500); // 최대 큐 크기
        executor.setThreadNamePrefix("StatsAsync-"); // 로그에서 쓰레드관련 식별
        executor.initialize();
        return executor;
    }
}
