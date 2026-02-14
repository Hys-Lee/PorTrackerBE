package com.PorTracker.PorTrackerBE.domain.memo.service;

import com.PorTracker.PorTrackerBE.domain.memo.dto.MemoCreateRequest;
import com.PorTracker.PorTrackerBE.domain.memo.entity.MemoRecord;
import com.PorTracker.PorTrackerBE.global.constant.SqliteSchema;
import com.PorTracker.PorTrackerBE.service.sqlite.SqliteDatabaseManager;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemoService {
    private final SqliteDatabaseManager sqliteManager;

    public List<MemoRecord> getAllMemos(String userId) {
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        String sql =
                String.format(
                        "SELECT %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s FROM %s",
                        SqliteSchema.COL_ID,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_CREATED_AT,
                        SqliteSchema.COL_UPDATED_AT,
                        SqliteSchema.COL_DELETED_AT,
                        SqliteSchema.COL_IMPORTANCE,
                        SqliteSchema.COL_TITLE,
                        SqliteSchema.COL_CONTENT,
                        SqliteSchema.COL_EVALUATION,
                        SqliteSchema.COL_DATE,
                        SqliteSchema.COL_MEMO_TYPE,
                        SqliteSchema.COL_ACTUAL_ID,
                        SqliteSchema.COL_TARGET_ID,
                        SqliteSchema.TABLE_MEMO);

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        MemoRecord.builder()
                                .id(rs.getLong(SqliteSchema.COL_ID))
                                .publicId(rs.getString(SqliteSchema.COL_PUBLIC_ID))
                                .createdAt(rs.getString(SqliteSchema.COL_CREATED_AT))
                                .updatedAt(rs.getString(SqliteSchema.COL_UPDATED_AT))
                                .deletedAt(rs.getString(SqliteSchema.COL_DELETED_AT))
                                .importance(rs.getString(SqliteSchema.COL_IMPORTANCE))
                                .title(rs.getString(SqliteSchema.COL_TITLE))
                                .content(rs.getString(SqliteSchema.COL_CONTENT))
                                .evaluation(rs.getString(SqliteSchema.COL_EVALUATION))
                                .date(rs.getString(SqliteSchema.COL_DATE))
                                .memoType(rs.getString(SqliteSchema.COL_MEMO_TYPE))
                                .actualId(rs.getLong(SqliteSchema.COL_ACTUAL_ID))
                                .targetId(rs.getLong(SqliteSchema.COL_TARGET_ID))
                                .build());
    }

    public MemoRecord getMemoById(String userId, String publicId) {
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        String sql =
                String.format(
                        "SELECT %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s FROM %s WHERE %s = ?",
                        SqliteSchema.COL_ID,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_CREATED_AT,
                        SqliteSchema.COL_UPDATED_AT,
                        SqliteSchema.COL_DELETED_AT,
                        SqliteSchema.COL_IMPORTANCE,
                        SqliteSchema.COL_TITLE,
                        SqliteSchema.COL_CONTENT,
                        SqliteSchema.COL_EVALUATION,
                        SqliteSchema.COL_DATE,
                        SqliteSchema.COL_MEMO_TYPE,
                        SqliteSchema.COL_ACTUAL_ID,
                        SqliteSchema.COL_TARGET_ID,
                        SqliteSchema.TABLE_MEMO,
                        SqliteSchema.COL_PUBLIC_ID);

        try {
            List<MemoRecord> results =
                    jdbcTemplate.query(
                            sql,
                            ps -> {
                                ps.setString(1, publicId);
                            },
                            (rs, rowNum) ->
                                    MemoRecord.builder()
                                            .id(rs.getLong(SqliteSchema.COL_ID))
                                            .publicId(rs.getString(SqliteSchema.COL_PUBLIC_ID))
                                            .createdAt(rs.getString(SqliteSchema.COL_CREATED_AT))
                                            .updatedAt(rs.getString(SqliteSchema.COL_UPDATED_AT))
                                            .deletedAt(rs.getString(SqliteSchema.COL_DELETED_AT))
                                            .importance(rs.getString(SqliteSchema.COL_IMPORTANCE))
                                            .title(rs.getString(SqliteSchema.COL_TITLE))
                                            .content(rs.getString(SqliteSchema.COL_CONTENT))
                                            .evaluation(rs.getString(SqliteSchema.COL_EVALUATION))
                                            .date(rs.getString(SqliteSchema.COL_DATE))
                                            .memoType(rs.getString(SqliteSchema.COL_MEMO_TYPE))
                                            .actualId(rs.getLong(SqliteSchema.COL_ACTUAL_ID))
                                            .targetId(rs.getLong(SqliteSchema.COL_TARGET_ID))
                                            .build());

            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            log.error("Memo 조회 중 에러 발생: {}", e.getMessage());
            return null;
        }
    }

    @Transactional
    public void addMemo(String userId, MemoCreateRequest request) {
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        String sql =
                String.format(
                        "INSERT OR IGNORE INTO %s (%s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        SqliteSchema.TABLE_MEMO,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_TITLE,
                        SqliteSchema.COL_CONTENT,
                        SqliteSchema.COL_IMPORTANCE,
                        SqliteSchema.COL_EVALUATION,
                        SqliteSchema.COL_DATE,
                        SqliteSchema.COL_MEMO_TYPE,
                        SqliteSchema.COL_ACTUAL_ID);

        String publicId = UUID.randomUUID().toString();

        jdbcTemplate.update(
                sql,
                ps -> {
                    ps.setString(1, publicId);
                    ps.setString(2, request.getTitle());
                    ps.setString(3, request.getContent());
                    ps.setString(4, request.getImportance());
                    ps.setString(5, request.getEvaluation());
                    ps.setString(6, request.getDate());
                    ps.setString(7, request.getMemoType());
                    ps.setObject(8, request.getActualId());
                });

        log.info("memo recorded successfully for user: {}", userId);
    }
}
