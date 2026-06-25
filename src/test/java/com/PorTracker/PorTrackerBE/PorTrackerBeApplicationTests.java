package com.PorTracker.PorTrackerBE;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:sqlite::memory:",
    "spring.datasource.driver-class-name=org.sqlite.JDBC",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.flyway.enabled=false",
    "SENTRY_DSN=http://mock-sentry-dsn@sentry.io/1",
    "REDIS_PASSWORD=mock-redis-pw",
    "SUPABASE_URL=http://mock-supabase-url",
    "SUPABASE_KEY=mock-supabase-key",
    "SUPABASE_JWT_SECRET=mock-jwt-secret-with-long-dummy-key-value-over-32bytes-size!",
    "google.client.id=mock-google-client-id",
    "google.client.secret=mock-google-client-secret",
    "GOOGLE_CLIENT_ID=mock-google-client-id",
    "GOOGLE_CLIENT_SECRET=mock-google-client-secret",
    "supabase.service.role.key=mock-supabase-service-role-key",
    "SUPABASE_SERVICE_ROLE_KEY=mock-supabase-service-role-key",
    "CLIENT_URL=http://mock-client-url",
    "client.url=http://mock-client-url"
})
class PorTrackerBeApplicationTests {

    @MockBean
    private RedissonClient redissonClient;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void contextLoads() {}
}
