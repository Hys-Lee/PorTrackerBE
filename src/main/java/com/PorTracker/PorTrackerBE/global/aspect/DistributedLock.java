package com.PorTracker.PorTrackerBE.global.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    String key(); // 락 이름

    long waitTime() default 5L; // 대기 (초)

    long leaseTime() default 3L; // 점유 (초)
}
