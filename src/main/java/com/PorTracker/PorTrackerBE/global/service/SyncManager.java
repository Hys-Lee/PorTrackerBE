package com.PorTracker.PorTrackerBE.global.service;

import com.PorTracker.PorTrackerBE.global.infra.sqlite.SqliteDatabaseManager;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncManager {
    private final SyncService syncService;
    private final SqliteDatabaseManager sqliteManager;

    // 512MB RAM 서버 환경을 고려한 최대 동시 활성 유저 DB 수 제한
    private static final int MAX_ACTIVE_USERS = 20;

    // 수정된 유저 목록 (dirty flag)
    private final Set<String> dirtyUsers = ConcurrentHashMap.newKeySet();

    // 유저별 마지막 활동 시간 기록 (LRU 판단 기준)
    private final Map<String, Instant> lastAccessMap = new ConcurrentHashMap<>();

    public void markDirty(String userId) {
        dirtyUsers.add(userId);
        registerOrUpdateAccess(userId);
    }

    /** 유저의 SQLite 메모리 세션 접근 시간을 갱신하고, 한도(MAX_ACTIVE_USERS) 초과 시 LRU 방출을 트리거합니다. */
    public synchronized void registerOrUpdateAccess(String userId) {
        lastAccessMap.put(userId, Instant.now());

        // 현재 활성화된 DB 커넥션 풀 수가 한계를 넘어서면, 가장 오랫동안 참조되지 않은 세션 방출
        if (lastAccessMap.size() > MAX_ACTIVE_USERS) {
            evictLeastRecentlyUsedUser(userId);
        }
    }

    private void evictLeastRecentlyUsedUser(String currentUserId) {
        String lruUserId = null;
        Instant oldestAccess = Instant.now();

        for (Map.Entry<String, Instant> entry : lastAccessMap.entrySet()) {
            String uId = entry.getKey();
            if (uId.equals(currentUserId)) {
                continue; // 방금 접속한 유저는 방출 대상에서 보호
            }
            if (entry.getValue().isBefore(oldestAccess)) {
                oldestAccess = entry.getValue();
                lruUserId = uId;
            }
        }

        if (lruUserId != null) {
            log.info(
                    "[LRU Eviction] Active user limit ({}) reached. Evicting LRU user: {}",
                    MAX_ACTIVE_USERS,
                    lruUserId);
            evictUser(lruUserId);
        }
    }

    private void evictUser(String userId) {
        try {
            // 방출 전 변경 이력(dirty flag)이 있다면 구글 드라이브에 최종 안전 백업
            if (dirtyUsers.contains(userId)) {
                syncService.uploadToCloud(userId);
                dirtyUsers.remove(userId);
            }
        } catch (Exception e) {
            log.error("[LRU Eviction] Failed to backup before evicting user: {}", userId, e);
        } finally {
            // HikariDataSource 커넥션 풀 해제 -> SQLite 인메모리 DB가 메모리상에서 즉시 완전 소멸됨
            sqliteManager.removeDataSource(userId);
            lastAccessMap.remove(userId);
            log.info("[LRU Eviction] Successfully evicted user: {} and reclaimed RAM", userId);
        }
    }

    @Scheduled(fixedDelay = 60000) // 1분 단위 백업 체크
    public void scheduledUpload() {
        if (dirtyUsers.isEmpty()) return;

        log.info("[SyncTask] Starting scheduled upload");
        for (String userId : dirtyUsers) {
            try {
                syncService.uploadToCloud(userId);
                dirtyUsers.remove(userId);
            } catch (Exception e) {
                log.error("[SyncTask] Failed to upload for user: {}", userId, e.getMessage());
            }
        }
    }

    @Scheduled(fixedDelay = 600000) // 10분마다 비활성 세션 감시
    public void cleanupIdleFiles() {
        Instant now = Instant.now();
        log.info("[CleanupTask] Checking for idle memory DB sessions...");

        for (String userId : lastAccessMap.keySet()) {
            Instant lastAccess = lastAccessMap.get(userId);
            // 마지막 접근으로부터 30분이 지난 경우 메모리 반환
            if (Duration.between(lastAccess, now).toMinutes() >= 30) {
                log.info("[CleanupTask] User {} session is idle. Reclaiming memory...", userId);
                evictUser(userId);
            }
        }
    }
}
