package com.PorTracker.PorTrackerBE.service;

// 이 한 줄이 있어야 .type(...)을 쓸 수 있음
import com.PorTracker.PorTrackerBE.global.config.*;
import com.PorTracker.PorTrackerBE.global.constant.FileConstants;
import com.PorTracker.PorTrackerBE.global.constant.SqliteSchema;
import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import java.io.File;
import java.nio.file.Paths;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
// import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;



@Service
@Slf4j
@RequiredArgsConstructor
public class SqliteDatabaseManager {

    private final AsyncConfig asyncConfig;


    // private final GoogleDriveService googleDriveService;

    // public DataSource getDataSourceForUser(String userId, String fileld, String userBaseCurrency,
    // String token) {
    public DataSource getDataSourceForUser(String userId, String fileld, String userBaseCurrency,
            String token) {
        // 경로 설정
        String dbPath = Paths.get(FileConstants.DB_STORAGE_ROOT, userId + ".db").toString();
        // File dbFile = new File(dbPath);

        // try {

        // // 파일 동기화
        // if (!dbFile.exists()
        // || googleDriveService.isNewerVersionAvailable(fileld, dbPath, token)) {
        // log.info("user[{}]'s db file downloaded from google drive", userId);
        // googleDriveService.downloadDatabaseFile(fileld, dbPath, token);
        // }
        // } catch (Exception e) {
        // log.error("error occured white db sync: {}", e.getMessage());
        // throw new BusinessException(ErrorCode.FILE_DOWNLOAD_FAILED);
        // }

        // datasource생성
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(FileConstants.SQLITE_DRIVER);
        dataSource.setUrl(FileConstants.JDBC_PREFIX + dbPath);



        // // sqlite용 datasource 빌드하기
        // DataSource dataSource =
        // DataSourceBuilder.create().driverClassName(FileConstants.SQLITE_DRIVER)
        // .url(FileConstants.JDBC_PREFIX + dbPath)
        // .type(org.sqlite.SQLiteDataSource.class).build();

        runFlywayMigration(dataSource);

        initializeUserCurrency(dataSource, userBaseCurrency);

        return dataSource;
    }

    // public void createInitialDatabase(String tempPath) {

    // File directory = new File(FileConstants.DB_STORAGE_ROOT);
    // if (!directory.exists())
    // directory.mkdirs();

    // DataSource dataSource =
    // DataSourceBuilder.create().driverClassName(FileConstants.SQLITE_DRIVER)
    // .url(FileConstants.JDBC_PREFIX + tempPath).build();

    // log.info("inject init schema to new sqlite db file: {}", tempPath);
    // runFlywayMigration(dataSource);
    // }
    public void createInitialFile(String localPath) {

        // 로컬 파일 생성할 디렉토리 만들기
        File dbFile = new File(localPath);
        File parentDir = dbFile.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (created) {
                log.info("DB directory made! :{}", parentDir.getAbsolutePath());
            } else {
                log.error("failed to make driectory: {}", parentDir.getAbsolutePath());
            }
        }


        // SQLite는 연결하면 파일이 생성된다는뎅

        DataSource dataSource =
                // DataSourceBuilder.create().driverClassName(FileConstants.SQLITE_DRIVER)
                // .url(FileConstants.JDBC_PREFIX + localPath).build();
                getDataSource(localPath);

        runFlywayMigration(dataSource);
        log.info("injected init schema to new sqlite db file: {}", localPath);
    }

    // DB 연결 객체(DataSource) 반환
    public DataSource getDataSource(String localPath) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(FileConstants.SQLITE_DRIVER);
        dataSource.setUrl(FileConstants.JDBC_PREFIX + localPath);
        return dataSource;

        // return DataSourceBuilder.create().driverClassName(FileConstants.SQLITE_DRIVER)
        // .url(FileConstants.JDBC_PREFIX + localPath).build();
    }

    private void initializeUserCurrency(DataSource dataSource, String userBaseCurrency) {
        JdbcTemplate sqliteJdbc = new JdbcTemplate(dataSource);

        // base currency가 'USD' 가 아닐 경우에만 삽입
        if (!"USD".equalsIgnoreCase(userBaseCurrency)) {
            log.info("try to insert user base currency: {}", userBaseCurrency);
            // String sql = " INSERT OR IGNORE INTO currency_type (code) VALUES (?)";
            String sql = String.format(" INSERT OR IGNORE INTO %s (%s) VALUES (?)",
                    SqliteSchema.TBL_CURRENCY_TYPE, SqliteSchema.COL_CODE);
            sqliteJdbc.update(sql, userBaseCurrency.toUpperCase());
        }
    }

    private void runFlywayMigration(DataSource dataSource) {
        try {

            Flyway flyway = Flyway.configure().dataSource(dataSource)
                    .locations("db/migration/sqlite").baselineOnMigrate(true).load();

            flyway.migrate();
            log.info("SQLite migration success");
        } catch (Exception e) {
            log.error("flyway migration failed: {}", e.getMessage());
            throw new BusinessException(ErrorCode.DB_SYNC_FAILED);
        }
    }
}
