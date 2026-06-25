package com.PorTracker.PorTrackerBE.global.infra.kafka;

import com.PorTracker.PorTrackerBE.global.common.ReplayContextHolder;
import com.PorTracker.PorTrackerBE.global.util.EncryptionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaReplayService {

    private final ConsumerFactory<String, String> consumerFactory;
    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;
    private final EncryptionUtils encryptionUtils;

    private static final String TOPIC = "user-transaction-logs";

    /**
     * 최종 동기화 시간(lastSyncAt) 이후의 변경 내용을 카프카 메시지로부터 복구(Replay)합니다.
     */
    public void replayUserEvents(String userId, Instant lastSyncAt) {
        log.info("[Replay] Starting Kafka event replay for user: {} from time: {}", userId, lastSyncAt);

        // AOP 순환 호출 방지를 위한 ThreadLocal 플래그 설정
        ReplayContextHolder.setReplaying(true);

        try (Consumer<String, String> consumer = consumerFactory.createConsumer()) {
            List<PartitionInfo> partitions = consumer.partitionsFor(TOPIC);
            if (partitions == null || partitions.isEmpty()) {
                log.warn("[Replay] Topic {} has no partitions or does not exist.", TOPIC);
                return;
            }

            // 모든 파티션을 뒤져서 해당 유저(Key = userId)의 특정 타임스탬프 이후 메시지를 수집
            List<TopicPartition> topicPartitions = new ArrayList<>();
            for (PartitionInfo p : partitions) {
                topicPartitions.add(new TopicPartition(TOPIC, p.partition()));
            }

            consumer.assign(topicPartitions);

            // 타임스탬프 기반 오프셋 획득 설정
            long searchTimestamp = lastSyncAt.toEpochMilli();
            Map<TopicPartition, Long> timestampsToSearch = new HashMap<>();
            for (TopicPartition tp : topicPartitions) {
                timestampsToSearch.put(tp, searchTimestamp);
            }

            Map<TopicPartition, OffsetAndTimestamp> offsets = consumer.offsetsForTimes(timestampsToSearch);

            // 검색된 오프셋으로 컨슈머 위치 강제 세팅 (seek)
            for (TopicPartition tp : topicPartitions) {
                OffsetAndTimestamp offsetAndTimestamp = offsets.get(tp);
                if (offsetAndTimestamp != null) {
                    consumer.seek(tp, offsetAndTimestamp.offset());
                    log.info("[Replay] Partition {} seeked to offset: {}", tp.partition(), offsetAndTimestamp.offset());
                } else {
                    // 해당 타임스탬프 이후 데이터가 없는 파티션은 끝으로 seek
                    consumer.seekToEnd(Collections.singletonList(tp));
                }
            }

            // 메시지 폴링 및 Replay 처리 (최대 3초 대기)
            int totalReplayed = 0;
            boolean keepPolling = true;
            int emptyPollCount = 0;

            while (keepPolling && emptyPollCount < 3) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                if (records.isEmpty()) {
                    emptyPollCount++;
                    continue;
                }
                emptyPollCount = 0;

                for (ConsumerRecord<String, String> record : records) {
                    // 나 외의 다른 유저 이벤트는 스킵 (Key = userId)
                    if (record.key() == null || !record.key().equals(userId)) {
                        continue;
                    }

                    // 복원 데이터 파싱 및 리플렉션 실행
                    try {
                        String decryptedValue = encryptionUtils.decrypt(record.value());
                        Map<String, Object> event = objectMapper.readValue(decryptedValue, Map.class);
                        executeReplayEvent(event);
                        totalReplayed++;
                    } catch (Exception ex) {
                        log.error("[Replay] Failed to replay event at offset {}", record.offset(), ex);
                    }
                }

                // 폴링 대상 메시지를 전부 소모했거나 오프셋 끝에 도달했을 경우 탈출
                keepPolling = false;
                for (TopicPartition tp : topicPartitions) {
                    long currentPosition = consumer.position(tp);
                    consumer.seekToEnd(Collections.singletonList(tp));
                    long endOffset = consumer.position(tp);
                    consumer.seek(tp, currentPosition);
                    
                    if (currentPosition < endOffset) {
                        keepPolling = true;
                        break;
                    }
                }
            }

            log.info("[Replay] Successfully replayed {} transaction events for user: {}", totalReplayed, userId);

        } catch (Exception e) {
            log.error("[Replay] Fatal error occurred during Kafka event replay for user: {}", userId, e);
        } finally {
            // AOP 플래그 해제
            ReplayContextHolder.clear();
        }
    }

    private void executeReplayEvent(Map<String, Object> event) throws Exception {
        String serviceName = (String) event.get("serviceName");
        String methodName = (String) event.get("methodName");
        List<Object> rawArgs = (List<Object>) event.get("arguments");
        List<String> argTypes = (List<String>) event.get("argumentTypes");

        // 첫 글자 소문자 처리로 스프링 빈 이름 추출
        String beanName = Character.toLowerCase(serviceName.charAt(0)) + serviceName.substring(1);
        Object serviceBean = applicationContext.getBean(beanName);

        // 메서드 및 매개변수 클래스 타입 수집
        Class<?>[] paramClasses = new Class<?>[argTypes.size()];
        Object[] typedArgs = new Object[rawArgs.size()];

        for (int i = 0; i < argTypes.size(); i++) {
            String typeName = argTypes.get(i);
            if (typeName.equals("null")) {
                paramClasses[i] = Object.class;
                typedArgs[i] = null;
            } else {
                Class<?> clazz = Class.forName(typeName);
                paramClasses[i] = clazz;
                // Jackson 역직렬화 가공
                typedArgs[i] = objectMapper.convertValue(rawArgs.get(i), clazz);
            }
        }

        // Reflection을 통한 메서드 호출 복원 (Redo)
        // 오버로딩 메서드가 있을 수 있으므로 getMethod로 시그니처 획득
        Method targetMethod = getCompatibleMethod(serviceBean.getClass(), methodName, paramClasses);
        targetMethod.invoke(serviceBean, typedArgs);
        
        log.info("[Replay] Replayed transaction event: {}.{}", serviceName, methodName);
    }

    private Method getCompatibleMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes) throws NoSuchMethodException {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            // 다형성이나 프록시 래핑 등으로 인해 완벽 매칭 실패 시 호환 메서드 검색
            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(methodName) && m.getParameterCount() == paramTypes.length) {
                    boolean compatible = true;
                    Class<?>[] methodParamTypes = m.getParameterTypes();
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (paramTypes[i] != Object.class && !methodParamTypes[i].isAssignableFrom(paramTypes[i])) {
                            compatible = false;
                            break;
                        }
                    }
                    if (compatible) return m;
                }
            }
            throw e;
        }
    }
}
