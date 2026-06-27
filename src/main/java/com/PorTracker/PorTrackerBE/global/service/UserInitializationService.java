package com.PorTracker.PorTrackerBE.global.service;

import com.PorTracker.PorTrackerBE.domain.profile.repository.ProfileRepository;
import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import com.PorTracker.PorTrackerBE.global.infra.kafka.KafkaReplayService;
import com.PorTracker.PorTrackerBE.global.infra.sqlite.SqliteDatabaseManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInitializationService {
    private final SqliteDatabaseManager sqliteManager;
    private final SyncService syncService;
    private final ProfileRepository profileRepository;
    private final KafkaReplayService kafkaReplayService;

    @Value("${app.db.version:1}")
    private int appDbVersion;

    public void initializeUserDatabase(String userId) {
        ensureDatabaseDirectoryExists();

        // 1. 메모리 DB 로딩 유무 체크 (커넥션을 맺어 currency_type 데이터 검사)
        boolean cacheExists = false;
        try {
            sqliteManager.getJdbcTemplate(userId).execute("SELECT 1 FROM currency_type LIMIT 1");
            cacheExists = true;
        } catch (Exception e) {
            log.info(
                    "[Init] Memory database cache missing for user: {}. Restoring from Cloud...",
                    userId);
        }

        if (!cacheExists) {
            // 메모리 캐시 리소스가 없을 시 GDrive로부터 새로 수집
            sqliteManager.removeDataSource(userId);
            syncService.downloadFromCloud(userId);

            // 1-2. 구글 드라이브 스냅샷 다운로드 직후 Kafka WAL 재플레이(Replay / Redo) 수행
            Instant lastSyncAt = Instant.EPOCH;
            try {
                var profileOpt = profileRepository.findById(UUID.fromString(userId));
                if (profileOpt.isPresent() && profileOpt.get().getUpdatedAt() != null) {
                    lastSyncAt = profileOpt.get().getUpdatedAt().toInstant();
                }
            } catch (Exception e) {
                log.warn("[Init] Failed to fetch sync time, replay might start from epoch", e);
            }

            kafkaReplayService.replayUserEvents(userId, lastSyncAt);
        }

        // 2. Supabase에서 유저의 최종 스키마 버전 정보 획득
        int userVersion = 0;
        try {
            var profileOpt = profileRepository.findById(UUID.fromString(userId));
            if (profileOpt.isPresent()) {
                userVersion = profileOpt.get().getUserDbVersion();
            }
        } catch (Exception e) {
            log.warn("[Init] Failed to fetch profile metadata for user: {}", userId, e);
        }

        // 3. 버전 대조를 통한 Flyway 0ms 우회 기법 적용
        if (userVersion == appDbVersion && cacheExists) {
            log.info(
                    "[Init] DB version matches ({}) for user: {}. Skipping Flyway validation.",
                    appDbVersion,
                    userId);
        } else {
            log.info(
                    "[Init] DB version mismatch (User: {}, App: {}). Executing Flyway migration...",
                    userVersion,
                    appDbVersion);

            // Flyway 마이그레이션 실행
            runFlywayMigration(userId);

            // Supabase에 최신 버전 업데이트
            try {
                profileRepository.updateUserDbVersion(UUID.fromString(userId), appDbVersion);
                log.info(
                        "[Init] Updated user_db_version to {} in Supabase for user: {}",
                        appDbVersion,
                        userId);
            } catch (Exception e) {
                log.error("[Init] Failed to update user_db_version in Supabase", e);
            }

            // 변경된 스키마 본을 드라이브로 백업
            try {
                syncService.uploadToCloud(userId);
            } catch (Exception e) {
                log.warn("[Init] Post-migration GDrive backup failed: {}", e.getMessage());
            }
        }
    }

    private void ensureDatabaseDirectoryExists() {
        Path dbFolderPath = Paths.get("db").toAbsolutePath();
        try {
            if (!Files.exists(dbFolderPath)) {
                Files.createDirectories(dbFolderPath);
                log.info("[Init] Created database directory: {}", dbFolderPath);
            }
        } catch (IOException e) {
            log.error("[Init] Failed to create db directory", e);
            throw new BusinessException(ErrorCode.DATA_CREATE_FAILED);
        }
    }

    private void runFlywayMigration(String userId) {
        DataSource dataSource = sqliteManager.getDataSource(userId);

        // SQLite 전용 Flyway 설정 빌드 및 실행
        Flyway flyway =
                Flyway.configure()
                        .dataSource(dataSource)
                        .locations("classpath:db/migration/sqlite")
                        .baselineOnMigrate(true)
                        .load();

        flyway.migrate();
        log.info("[Init] Flyway migration successful for user:{} ", userId);
    }
}
