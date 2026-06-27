package com.PorTracker.PorTrackerBE.global.infra.kafka;

import com.PorTracker.PorTrackerBE.global.util.EncryptionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTransactionLogProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final EncryptionUtils encryptionUtils;

    private static final String TOPIC = "user-transaction-logs";

    /** DB 변경 이력 로그를 카프카에 동기식(Sync Send)으로 전송하여 영속성을 확보합니다. */
    public void sendTransactionLog(
            String userId, String actionType, String sqlQuery, Object[] params) {
        try {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("userId", userId);
            messageMap.put("actionType", actionType);
            messageMap.put("sqlQuery", sqlQuery);
            messageMap.put("params", params);
            messageMap.put("timestamp", System.currentTimeMillis());

            String payload = objectMapper.writeValueAsString(messageMap);
            String encryptedPayload = encryptionUtils.encrypt(payload);

            // userId를 메시지 Key로 주어 동일 유저의 쿼리 순서(Partition FIFO) 보장
            // .get()을 호출하여 카프카 브로커의 적재 Ack를 기다리는 동기식 처리 수행
            kafkaTemplate.send(TOPIC, userId, encryptedPayload).get();

            log.debug(
                    "[KafkaWAL] Sent transaction log for user: {}, action: {}", userId, actionType);
        } catch (Exception e) {
            log.error(
                    "[KafkaWAL] Failed to write commit log synchronously for user: {}", userId, e);
            throw new RuntimeException("WAL 영속화 실패 - 트랜잭션을 보장할 수 없습니다.", e);
        }
    }

    public void sendServiceEvent(
            String userId, String serviceName, String methodName, Object[] args) {
        try {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("userId", userId);
            messageMap.put("serviceName", serviceName);
            messageMap.put("methodName", methodName);

            String[] argTypes = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                argTypes[i] = args[i] != null ? args[i].getClass().getName() : "null";
            }

            messageMap.put("arguments", args);
            messageMap.put("argumentTypes", argTypes);
            messageMap.put("timestamp", System.currentTimeMillis());

            String payload = objectMapper.writeValueAsString(messageMap);
            String encryptedPayload = encryptionUtils.encrypt(payload);

            kafkaTemplate.send(TOPIC, userId, encryptedPayload).get();

            log.info(
                    "[KafkaWAL] Sent service event WAL for user: {}, method: {}.{}",
                    userId,
                    serviceName,
                    methodName);
        } catch (Exception e) {
            log.error("[KafkaWAL] Failed to write service event WAL for user: {}", userId, e);
            throw new RuntimeException("WAL 서비스 이벤트 영속화 실패", e);
        }
    }
}
