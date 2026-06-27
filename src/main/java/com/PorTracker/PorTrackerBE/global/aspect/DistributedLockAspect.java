package com.PorTracker.PorTrackerBE.global.aspect;

import com.PorTracker.PorTrackerBE.global.annotation.DistributedLock;
import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import com.PorTracker.PorTrackerBE.global.util.CustomSpringELParser;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAspect {
    private final RedissonClient redissonClient;

    @Around("@annotation(distributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock)
            throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        // SpEL을 이용해 동적으로 락 키 획득 (예: lock:sync:user-123)
        String dynamicKey =
                "lock:"
                        + CustomSpringELParser.getDynamicValue(
                                parameterNames, args, distributedLock.key());
        RLock lock = redissonClient.getLock(dynamicKey);

        log.debug(
                "[RedissonLock] Attempting to acquire lock for key: {}, waitTime: {}s, leaseTime: {}s",
                dynamicKey,
                distributedLock.waitTime(),
                distributedLock.leaseTime());

        try {
            boolean available =
                    lock.tryLock(
                            distributedLock.waitTime(),
                            distributedLock.leaseTime(),
                            TimeUnit.SECONDS);

            if (!available) {
                log.warn(
                        "[RedissonLock] Lock acquisition failed for key: {} (Conflict / Duplicate Request detected)",
                        dynamicKey);
                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
            }

            log.debug("[RedissonLock] Successfully acquired lock for key: {}", dynamicKey);
            return joinPoint.proceed(); // 비즈니스 메서드 실행
        } finally {
            // 내가 획득한 락만 해제
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("[RedissonLock] Released lock for key: {}", dynamicKey);
            }
        }
    }
}
