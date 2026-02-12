package com.PorTracker.PorTrackerBE.service.sqlite;

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
// import org.flywaydb.core.internal.database.sqlite.SQLiteSchema;
// import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;



@Service
@Slf4j
@RequiredArgsConstructor
public class SqliteDatabaseManager {

    // private final AsyncConfig asyncConfig;

    // 초기 생성시
    public void createInitialFile(String localPath) {

        ensureDirectoryExists(localPath);

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(FileConstants.SQLITE_DRIVER);
        dataSource.setUrl(FileConstants.JDBC_PREFIX + localPath);

        runFlywayDBMigration(dataSource);
        log.info("initially sqlite db created at: {}", localPath);
    }


    public JdbcTemplate getJdbcTemplateOfDataSource(String userId, String userBaseCurrency) {
        String dbPath = Paths.get(FileConstants.DB_STORAGE_ROOT, userId + ".db").toString();

        // 디렉토리 없으면 생성
        ensureDirectoryExists(dbPath);

        // 접속 정보 생성
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(FileConstants.SQLITE_DRIVER);
        dataSource.setUrl(FileConstants.JDBC_PREFIX + dbPath);

        // 파일이 있었다면 업뎃, 없다면 테이블 생성
        runFlywayDBMigration(dataSource);

        // 기본 통화 설정(데이터 세팅)
        initializeUserCurrency(dataSource, userBaseCurrency);

        return new JdbcTemplate(dataSource);

    }


    // public DataSource getDataSourceForUser(String userId, String fileld, String userBaseCurrency,
    // String token) {
    // // 경로 설정
    // String dbPath = Paths.get(FileConstants.DB_STORAGE_ROOT, userId + ".db").toString();

    // // datasource생성
    // DriverManagerDataSource dataSource = new DriverManagerDataSource();
    // dataSource.setDriverClassName(FileConstants.SQLITE_DRIVER);
    // dataSource.setUrl(FileConstants.JDBC_PREFIX + dbPath);



    // runFlywayMigration(dataSource);

    // initializeUserCurrency(dataSource, userBaseCurrency);

    // return dataSource;
    // }



    // // SQLite는 연결하면 파일이 생성된다는뎅
    // DataSource dataSource = getDataSource(localPath);

    // runFlywayDBMigration(dataSource);
    // log.info("injected init schema to new sqlite db file: {}", localPath);
    // }

    private void ensureDirectoryExists(String dbPath) {
        File dbFile = new File(dbPath);
        File parentDir = dbFile.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (created) {
                log.info("DB directory made! :{}", parentDir.getAbsolutePath());
            } else {
                log.error("failed to make driectory: {}", parentDir.getAbsolutePath());
            }
        }
    }

    // // DB 연결 객체(DataSource) 반환
    // public DataSource getDataSource(String localPath) {
    // DriverManagerDataSource dataSource = new DriverManagerDataSource();
    // dataSource.setDriverClassName(FileConstants.SQLITE_DRIVER);
    // dataSource.setUrl(FileConstants.JDBC_PREFIX + localPath);
    // return dataSource;

    // // return DataSourceBuilder.create().driverClassName(FileConstants.SQLITE_DRIVER)
    // // .url(FileConstants.JDBC_PREFIX + localPath).build();
    // }

    private void initializeUserCurrency(DataSource dataSource, String userBaseCurrency) {
        JdbcTemplate sqliteJdbc = new JdbcTemplate(dataSource);

        // base currency가 'USD' 가 아닐 경우에만 삽입
        if (!"USD".equalsIgnoreCase(userBaseCurrency)) {
            log.info("try to insert user base currency: {}", userBaseCurrency);
            // String sql = " INSERT OR IGNORE INTO currency_type (code) VALUES (?)";
            String sql = String.format(" INSERT OR IGNORE INTO %s (%s) VALUES (?)",
                    SqliteSchema.TABLE_CURRENCY_TYPE, SqliteSchema.COL_CODE);
            sqliteJdbc.update(sql, userBaseCurrency.toUpperCase());
        }
    }

    private void runFlywayDBMigration(DataSource dataSource) {
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
