package com.PorTracker.PorTrackerBE.service;

import java.io.File;
import java.nio.file.Paths;
import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;
import com.PorTracker.PorTrackerBE.global.constant.FileConstants;
import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SqliteDatabaseManager {
    private final GoogleDriveService googleDriveService;

    public DataSource getDataSourceForUser(String userId, String fileld) {
        // 경로 설정
        String dbPath = Paths.get(FileConstants.DB_STORAGE_ROOT, userId + ".db").toString();
        File dbFile = new File(dbPath);

        try {

            // 파일 동기화
            if (!dbFile.exists() || googleDriveService.isNewerVersionAvailable(fileld, dbPath)) {
                log.info("user[{}]'s db file downloaded from google drive", userId);
                googleDriveService.downloadDatabaseFile(fileld, dbPath);
            }
        } catch (Exception e) {
            log.error("error occured white db sync: {}", e.getMessage());
            throw new BusinessException(ErrorCode.FILE_DOWNLOAD_FAILED);
        }
        // sqlite용 datasource 빌드하기
        return DataSourceBuilder.create().driverClassName(FileConstants.SQLITE_DRIVER)
                .url(FileConstants.JDBC_PREFIX + dbPath).build();
    }
}
