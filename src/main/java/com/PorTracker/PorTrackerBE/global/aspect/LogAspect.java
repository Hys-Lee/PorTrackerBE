package com.PorTracker.PorTrackerBE.global.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LogAspect {

    // service하위 패키지 모든 메서드 실행 시간 로깅
    @Around("execution(* com.PorTracker.PorTrackerBE.service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();

        long executoinTime = System.currentTimeMillis() - start;

        log.info(
                "[Performance] {} executed in {}ms",
                joinPoint.getSignature().getName(),
                executoinTime);

        return proceed;
    }
}
