package com.PorTracker.PorTrackerBE.global.infra.sqlite;

import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SqliteDatabaseManager {

    private final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();

    public DataSource getDataSource(String userId) {
        // Map에는 있는데 실제는 없다면, 캐시 무효화하고 새로 만들기
        Path path = Paths.get("db/" + userId + ".db");
        if (!Files.exists(path)) {
            removeDataSource(userId);
        }

        return dataSourceMap.computeIfAbsent(userId, this::createSqliteDataSource);
    }

    public JdbcTemplate getJdbcTemplate(String userId) {
        return new JdbcTemplate(getDataSource(userId));
    }

    private DataSource createSqliteDataSource(String userId) {
        try {
            Path dbFolderPath = Paths.get("db").toAbsolutePath();

            if (!Files.exists(dbFolderPath)) {
                Files.createDirectories(dbFolderPath);
                log.info("[SQLite] created base directory: {}", dbFolderPath);
            }

            String dbFilePath = dbFolderPath.resolve(userId + ".db").toString();

            // String dbUrl = "jdbc:sqlite:db/"+userId+".db";
            String dbUrl = "jdbc:sqlite:" + dbFilePath;

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            config.setDriverClassName("org.sqlite.JDBC");
            config.setPoolName("SqlitePool-" + userId);

            // Sqlite 최적화
            config.setMaximumPoolSize(1); // sqlite는 파일 단위 락킹이니.
            config.setConnectionTestQuery("SELECT 1");
            config.addDataSourceProperty("journal_mode", "WAL"); // 쓰기 성능 향상
            config.addDataSourceProperty("synchronous", "NORMAL");

            log.info("[SQLite] Created new DataSource for User:{}", userId);
            return new HikariDataSource(config);
        } catch (IOException e) {
            log.error("[SQLite] Failed to create db directory", e);
            throw new BusinessException(ErrorCode.DATA_CREATE_FAILED);
        }
    }

    public void removeDataSource(String userId) {
        DataSource ds = dataSourceMap.remove(userId);
        if (ds instanceof HikariDataSource) {
            ((HikariDataSource) ds).close();
            log.info("[SQLite] Closed DataSource for user: {}", userId);
        }
    }

    // bulk api 위해
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate(String userId) {
        return new NamedParameterJdbcTemplate(getDataSource(userId));
    }
}
