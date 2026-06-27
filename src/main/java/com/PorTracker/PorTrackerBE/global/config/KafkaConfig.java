package com.PorTracker.PorTrackerBE.global.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public NewTopic transactionLogsTopic() {
        return TopicBuilder.name("user-transaction-logs")
                .partitions(3) // 유저 ID 기준 해시 분산을 위해 멀티 파티션 구성
                .replicas(1) // 개발/배포 환경에 맞춤
                .build();
    }
}
