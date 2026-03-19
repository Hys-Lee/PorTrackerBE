package com.PorTracker.PorTrackerBE.global.aspect;

import com.PorTracker.PorTrackerBE.global.common.UserContextHolder;
import com.PorTracker.PorTrackerBE.global.service.SyncManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class SyncAspect {
    private final SyncManager syncManager;

    @AfterReturning("execution(* com.PorTracker.PorTrackerBE.domain..*Service.*(..))")
    public void afterServiceMethod() {
        String userId = UserContextHolder.getUserId();
        if (userId != null) {
            syncManager.markDirty(userId);
        }
    }
}
