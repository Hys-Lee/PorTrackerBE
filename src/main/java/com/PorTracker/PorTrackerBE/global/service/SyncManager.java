package com.PorTracker.PorTrackerBE.global.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.PorTracker.PorTrackerBE.global.infra.sqlite.SqliteDatabaseManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncManager {
    private final SyncService syncService;
    private final SqliteDatabaseManager sqliteManager;

     // 수정된 유저 목록 (dirty flag)
    private final Set<String> dirtyUsers = ConcurrentHashMap.newKeySet();

    // 유저별 마지막 활동 시간 기록
    private final Map<String, Instant> lastAccessMap = new ConcurrentHashMap<>();

    public void markDirty(String userId){
        dirtyUsers.add(userId);
        updateAccessTime(userId);
    }

    public void updateAccessTime(String userId){
        lastAccessMap.put(userId, Instant.now());
    }

    @Scheduled(fixedDelay = 10000) // 30만ms = 5분  // 테스트용 10초
    public void scheduledUpload(){
        if(dirtyUsers.isEmpty()) return;

        log.info("[SyncTask] Starting scheduled upload");
        for (String userId:dirtyUsers){
            try{
                syncService.uploadToCloud(userId);
                dirtyUsers.remove(userId);
            }catch(Exception e){
                log.error("[SyncTask] Failed to upload for user: {}", userId,e.getMessage());
            }
        }
    }


    @Scheduled(fixedDelay = 600000) // 10분마다 체크
    // 30분 활동 없으면 서버에서 삭제.(삭제 전 업로드)
    public void cleanupIdleFiles(){
        Instant now = Instant.now();

        log.info("[CleanupTask] chekcing for idle files...");

        for(String userId:lastAccessMap.keySet()){
            Instant lastAccess = lastAccessMap.get(userId);
            // 30분 idle일 시 삭제.
            if(Duration.between(lastAccess, now).toMinutes()>=30){
                log.info("[CleanupTask] user {} is idle. cleaning up...", userId);

                // 수정 사항 있다면, 마지막 업로드
                if(dirtyUsers.contains(userId)){
                    syncService.uploadToCloud(userId);
                    dirtyUsers.remove(userId);
                }

                // db연결 종료 및 파일 삭제
                sqliteManager.removeDataSource(userId);
                deleteLocalFile(userId);
                lastAccessMap.remove(userId);
            }


        }
    }

    private void deleteLocalFile(String userId){
        try{
            Path path = Paths.get("db/"+userId+".db");
            Files.deleteIfExists(path);
            Files.deleteIfExists(Paths.get(path.toString()+"-wal"));
            Files.deleteIfExists(Paths.get(path.toString()+"-shm"));
            log.info("[CleanupTask] Deleted local files for user: {}", userId);
        }catch(IOException e){
            log.error("[CleanupTask] Failed to delete files to user: {}", userId, e.getMessage());
        }
    }

}
