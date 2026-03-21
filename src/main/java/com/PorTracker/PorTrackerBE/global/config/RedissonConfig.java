package com.PorTracker.PorTrackerBE.global.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RedissonConfig {
    // @Value("${spring.data.redis.host}")
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String redissPw;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        String redisUrl = "rediss://" + host + ":" + port;
        // test
        // log.info("redisUrl: {}, pw:{}", redisUrl, redissPw);
        config.useSingleServer()
                .setAddress(redisUrl)
                .setPassword(redissPw)
                .setSslEnableEndpointIdentification(false);

        return Redisson.create(config);
    }
}
