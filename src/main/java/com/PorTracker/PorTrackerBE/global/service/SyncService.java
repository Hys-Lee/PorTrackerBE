package com.PorTracker.PorTrackerBE.global.service;

import com.PorTracker.PorTrackerBE.domain.profile.entity.CredentialRecord;
import com.PorTracker.PorTrackerBE.domain.profile.repository.CredentialRepository;
import com.PorTracker.PorTrackerBE.domain.statistic.service.StatisticService;
import com.PorTracker.PorTrackerBE.global.infra.google.GoogleAuthService;
import com.PorTracker.PorTrackerBE.global.infra.google.GoogleDriveClient;
import com.PorTracker.PorTrackerBE.global.infra.sqlite.SqliteDatabaseManager;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {
    // private final SupabaseAuthClient supabaseAuthClient;
    private final CredentialRepository credentialRepository;
    private final GoogleDriveClient googleDriveClient;
    private final GoogleAuthService googleAuthService;
    private final SqliteDatabaseManager sqliteManager;
    private final StatisticService statisticService;

    @CircuitBreaker(name = "gdriveDownload", fallbackMethod = "fallbackDownload")
    public void downloadFromCloud(String userId) {
        CredentialRecord cred =
                credentialRepository.findByUserId(UUID.fromString(userId)).orElse(null);
        if (cred == null || cred.getAccessToken() == null) {
            log.warn("[Sync] No credentials found for user: {}. Skipping dowload...", userId);
            return;
        }

        try {
            // 다운로드 시도
            performDownload(userId, cred.getAccessToken());

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.info("[Sync] Access Token Expried. Refreshing for user: {}", userId);

                String newAccessToken =
                        googleAuthService.refreshAccessToken(cred.getRefreshToken());

                if (newAccessToken != null) {
                    credentialRepository.updateAccessToken(UUID.fromString(userId), newAccessToken);

                    performDownload(userId, newAccessToken);
                    log.info("[Sync] Download dsuccessful after token refresh");

                } else {
                    log.error("[Sync] Google API Error during download:{}");
                }
            }
        } catch (Exception e) {
            log.error("[Sync] Unexpected error during download: {}", e.getMessage());
        }
    }

    private void performDownload(String userId, String accessToken) {
        String fileId = googleDriveClient.findFileIdByName(userId + ".db", accessToken);
        if (fileId != null) {
            byte[] content = googleDriveClient.downloadFile(fileId, accessToken);

            if (content != null) {
                Path tempPath = Paths.get("db/" + userId + "_temp.db");
                try {
                    // 1. 기존 활성 커넥션 해제
                    sqliteManager.removeDataSource(userId);

                    // 2. 임시 파일 쓰기
                    Path parentDir = tempPath.getParent();
                    if (parentDir != null && !Files.exists(parentDir)) {
                        Files.createDirectories(parentDir);
                    }
                    Files.write(tempPath, content);

                    // 3. 파일 -> 메모리 DB 복제(Restore)
                    restoreFileToMemory(tempPath.toAbsolutePath().toString(), userId);
                    log.info("[Sync] Downloaded and restored database for user: {}", userId);

                } catch (Exception e) {
                    log.error("[Sync] Database download/restore failed", e);
                } finally {
                    // 4. 임시 파일 정리
                    try {
                        Files.deleteIfExists(tempPath);
                    } catch (IOException ex) {
                        log.warn("[Sync] Failed to delete temp restore file", ex);
                    }
                }
            }
        }
    }

    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2.0))
    @CircuitBreaker(name = "gdriveUpload", fallbackMethod = "fallbackUpload")
    public void uploadToCloud(String userId) {
        Path tempPath = Paths.get("db/" + userId + ".db");
        try {
            // 1. 메모리 DB -> 임시 버퍼 파일 백업
            backupMemoryToFile(userId, tempPath.toAbsolutePath().toString());

            // 2. DB에서 토큰 가져오기
            CredentialRecord cred =
                    credentialRepository.findByUserId(UUID.fromString(userId)).orElse(null);

            if (cred == null || cred.getAccessToken() == null) {
                log.error("[Sync] No Credentials for user: {}", userId);
                return;
            }

            // 3. 업로드 시도 (401시 토큰 갱신)
            try {
                googleDriveClient.syncToDrive(userId, cred.getAccessToken());
                log.info("[Sync] Upload successful for user: {}", userId);

            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    log.info("[Sync] Token expired, refreshing for user: {}", userId);

                    String newAccessToken =
                            googleAuthService.refreshAccessToken(cred.getRefreshToken());

                    if (newAccessToken != null) {
                        credentialRepository.updateAccessToken(
                                UUID.fromString(userId), newAccessToken);
                        googleDriveClient.syncToDrive(userId, newAccessToken);
                        log.info("[Sync] Uploaded successfully after token refreshed");
                    }
                } else {
                    log.error(
                            "[Sync] Google API Client Error: {} - {}",
                            e.getStatusCode(),
                            e.getResponseBodyAsString());
                }
            }
        } catch (Exception e) {
            log.error("[Sync] Unexpected error during upload: {}", e.getMessage());
        } finally {
            // 4. 버퍼 파일 소거
            try {
                Files.deleteIfExists(tempPath);
                Files.deleteIfExists(Paths.get(tempPath.toString() + "-wal"));
                Files.deleteIfExists(Paths.get(tempPath.toString() + "-shm"));
                log.info("[Sync] Temp file cleaned up for user: {}", userId);
            } catch (IOException ex) {
                log.warn("[Sync] Failed to delete temp upload file", ex);
            }
        }

        try {
            statisticService.contributeTotalAsset(userId);
        } catch (Exception e) {
            log.warn("[Stat] Failed to contribute statistics: {}", e.getMessage());
        }
    }

    private void restoreFileToMemory(String sourceFilePath, String userId) throws Exception {
        // SQLite JDBC 드라이버에서 공식 지원하는 'restore from' 명령어를 통해 인메모리로 복원
        sqliteManager.getJdbcTemplate(userId).execute("restore from '" + sourceFilePath + "'");
        log.info("[Sync] Database restored to memory via SQL for user: {}", userId);
    }

    private void backupMemoryToFile(String userId, String targetFilePath) throws Exception {
        Path parentDir = Paths.get(targetFilePath).getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        // 백업 대상 파일이 이미 존재할 경우 덮어쓰기 위해 사전 삭제
        Files.deleteIfExists(Paths.get(targetFilePath));

        // 'backup to' 명령어를 통해 인메모리 데이터를 디스크 임시 파일로 백업
        sqliteManager.getJdbcTemplate(userId).execute("backup to '" + targetFilePath + "'");
        log.info("[Sync] Database backed up from memory to file via SQL for user: {}", userId);
    }

    public void fallbackDownload(String userId, Throwable t) {
        log.error(
                "[Fallback] GDrive download circuit open or failed. serving from memory database if exists. user: {}, reason: {}",
                userId,
                t.getMessage());
    }

    public void fallbackUpload(String userId, Throwable t) {
        log.error(
                "[Fallback] GDrive upload circuit open or failed after retries. WAL log is preserved. user: {}, reason: {}",
                userId,
                t.getMessage());
    }
}
