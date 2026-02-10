package com.PorTracker.PorTrackerBE.global.aspect;

import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
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
        String key = "lock:" + joinPoint.getArgs()[1]; // 임시
        RLock lock = redissonClient.getLock(key);

        try {
            boolean available =
                    lock.tryLock(
                            distributedLock.waitTime(),
                            distributedLock.leaseTime(),
                            TimeUnit.SECONDS);

            if (!available) {
                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
            }

            return joinPoint.proceed(); // 동작 메서드 실행
        } finally {
            // 내가 잡은 락에 대해 해제
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
