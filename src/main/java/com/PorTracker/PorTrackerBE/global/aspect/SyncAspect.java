package com.PorTracker.PorTrackerBE.global.aspect;

import com.PorTracker.PorTrackerBE.global.common.ReplayContextHolder;
import com.PorTracker.PorTrackerBE.global.common.UserContextHolder;
import com.PorTracker.PorTrackerBE.global.infra.kafka.KafkaTransactionLogProducer;
import com.PorTracker.PorTrackerBE.global.service.SyncManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class SyncAspect {
    private final SyncManager syncManager;
    private final KafkaTransactionLogProducer kafkaProducer;

    // @WalService 어노테이션이 지정된 클래스의 모든 public 메서드 호출 성공 후 Dirty 마크 지정
    @AfterReturning("@within(com.PorTracker.PorTrackerBE.global.annotation.WalService)")
    public void afterServiceMethod() {
        String userId = UserContextHolder.getUserId();
        if (userId != null && !ReplayContextHolder.isReplaying()) {
            syncManager.markDirty(userId);
        }
    }

    // @WalService가 지정된 클래스의 public 메서드 중, @Transactional의 readOnly가 false(쓰기 트랜잭션)인 메서드 실행 후 WAL 발행
    @AfterReturning(
            pointcut =
                    "@within(com.PorTracker.PorTrackerBE.global.annotation.WalService) && @annotation(transactional)",
            argNames = "joinPoint,transactional")
    public void afterCudMethod(JoinPoint joinPoint, Transactional transactional) {
        // readOnly = true인 조회 트랜잭션은 WAL 적재에서 제외
        if (transactional.readOnly()) {
            return;
        }

        String userId = UserContextHolder.getUserId();
        if (userId != null && !ReplayContextHolder.isReplaying()) {
            Class<?> targetClass =
                    org.springframework.util.ClassUtils.getUserClass(joinPoint.getTarget());
            String serviceName = targetClass.getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            Object[] args = joinPoint.getArgs();

            try {
                kafkaProducer.sendServiceEvent(userId, serviceName, methodName, args);
            } catch (Exception e) {
                log.error("[AOP] Failed to emit WAL transaction log", e);
                throw e; // 트랜잭션 롤백 보장
            }
        }
    }
}
