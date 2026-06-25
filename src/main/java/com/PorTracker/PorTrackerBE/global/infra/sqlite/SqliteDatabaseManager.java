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
        return dataSourceMap.computeIfAbsent(userId, this::createSqliteDataSource);
    }

    public JdbcTemplate getJdbcTemplate(String userId) {
        return new JdbcTemplate(getDataSource(userId));
    }

    private DataSource createSqliteDataSource(String userId) {
        // 인메모리 sqlite shared cache 모드 URL 구성
        String dbUrl = "jdbc:sqlite:file:memdb_" + userId + "?mode=memory&cache=shared";

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setDriverClassName("org.sqlite.JDBC");
        config.setPoolName("SqlitePool-" + userId);

        // Sqlite 최적화 및 풀 제한
        config.setMaximumPoolSize(1); // 동시성 격리 보장
        config.setConnectionTestQuery("SELECT 1");
        
        // 메모리 DB이므로 WAL과 NORMAL 옵션은 크게 무의미하나 드라이버 프로퍼티 호환성 유지
        config.addDataSourceProperty("journal_mode", "MEMORY"); 
        config.addDataSourceProperty("synchronous", "OFF");

        log.info("[SQLite] Created new In-Memory DataSource for User:{}", userId);
        return new HikariDataSource(config);
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
