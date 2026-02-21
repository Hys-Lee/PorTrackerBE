package com.PorTracker.PorTrackerBE.domain.memo.repository;

import com.PorTracker.PorTrackerBE.domain.memo.dto.MemoCreateRequest;
import com.PorTracker.PorTrackerBE.domain.memo.entity.MemoRecord;
import com.PorTracker.PorTrackerBE.global.constant.SqliteSchema;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MemoRepository {

    private static final String actualPortfolioPublicIdName = "actual_portfolio_public_id";
    private static final String targetPortfolioPublicIdName = "target_portfolio_public_id";

    private static final String BASE_SELECT_SQL =
            String.format(
                    " SELECT m.%s, m.%s, m.%s, m.%s, m.%s, m.%s, m.%s, m.%s, m.%s, m.%s,m.%s, ap.%s as %s, tp.%s as %s"
                            + " FROM %s m LEFT JOIN %s ap ON m.%s=ap.%s LEFT JOIN %s tp ON m.%s=tp.%s",
                    // select
                    SqliteSchema.COL_ID,
                    SqliteSchema.COL_PUBLIC_ID,
                    SqliteSchema.COL_CREATED_AT,
                    // SqliteSchema.COL_UPDATED_AT, SqliteSchema.COL_DELETED_AT,
                    SqliteSchema.COL_IMPORTANCE,
                    SqliteSchema.COL_TITLE,
                    SqliteSchema.COL_CONTENT,
                    SqliteSchema.COL_EVALUATION,
                    SqliteSchema.COL_DATE,
                    SqliteSchema.COL_MEMO_TYPE,
                    SqliteSchema.COL_ACTUAL_ID,
                    SqliteSchema.COL_TARGET_ID,
                    SqliteSchema.COL_PUBLIC_ID,
                    actualPortfolioPublicIdName,
                    SqliteSchema.COL_PUBLIC_ID,
                    targetPortfolioPublicIdName,

                    // from
                    SqliteSchema.TABLE_MEMO,
                    SqliteSchema.TABLE_ACTUAL_PORTFOLIO,
                    SqliteSchema.COL_ACTUAL_ID,
                    SqliteSchema.COL_ID,
                    SqliteSchema.TABLE_TARGET_PORTFOLIO,
                    SqliteSchema.COL_TARGET_ID,
                    SqliteSchema.COL_ID);

    private final RowMapper<MemoRecord> memoMapper =
            (rs, rowNum) ->
                    MemoRecord.builder()
                            .id(rs.getLong(SqliteSchema.COL_ID))
                            .publicId(rs.getString(SqliteSchema.COL_PUBLIC_ID))
                            .createdAt(rs.getString(SqliteSchema.COL_CREATED_AT))
                            // .updatedAt(rs.getString(SqliteSchema.COL_UPDATED_AT))
                            // .deletedAt(rs.getString(SqliteSchema.COL_DELETED_AT))
                            .importance(rs.getString(SqliteSchema.COL_IMPORTANCE))
                            .title(rs.getString(SqliteSchema.COL_TITLE))
                            .content(rs.getString(SqliteSchema.COL_CONTENT))
                            .evaluation(rs.getString(SqliteSchema.COL_EVALUATION))
                            .date(rs.getString(SqliteSchema.COL_DATE))
                            .memoType(rs.getString(SqliteSchema.COL_MEMO_TYPE))
                            .actualId(rs.getLong(SqliteSchema.COL_ACTUAL_ID))
                            .actualPublicId(rs.getString(actualPortfolioPublicIdName))
                            .targetId(rs.getLong(SqliteSchema.COL_TARGET_ID))
                            .targetPublicId(rs.getString(targetPortfolioPublicIdName))
                            .build();

    public List<MemoRecord> findAll(JdbcTemplate jdbcTemplate) {
        String sql = BASE_SELECT_SQL + " WHERE " + "m." + SqliteSchema.COL_DELETED_AT + " IS NULL";
        return jdbcTemplate.query(sql, memoMapper);
    }

    public Optional<MemoRecord> findByPublicId(JdbcTemplate jdbcTemplate, String publicId) {
        String sql =
                BASE_SELECT_SQL
                        + " WHERE "
                        + "m."
                        + SqliteSchema.COL_PUBLIC_ID
                        + "=? AND "
                        + "m."
                        + SqliteSchema.COL_DELETED_AT
                        + " IS NULL";
        return jdbcTemplate.query(sql, ps -> ps.setString(1, publicId), memoMapper).stream()
                .findFirst();
    }

    public Long save(
            JdbcTemplate jdbcTemplate,
            MemoCreateRequest request,
            String publicId,
            Long actualId,
            Long targetId) {
        // test
        log.info("actualId in save in emmoRepo: {}", actualId);

        String sql =
                String.format(
                        "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        SqliteSchema.TABLE_MEMO,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_IMPORTANCE,
                        SqliteSchema.COL_TITLE,
                        SqliteSchema.COL_CONTENT,
                        SqliteSchema.COL_EVALUATION,
                        SqliteSchema.COL_DATE,
                        SqliteSchema.COL_MEMO_TYPE,
                        SqliteSchema.COL_ACTUAL_ID,
                        SqliteSchema.COL_TARGET_ID);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                conn -> {
                    PreparedStatement ps = conn.prepareStatement(sql, new String[] {"id"});
                    ps.setString(1, publicId);
                    ps.setString(2, request.getImportance().getValue());
                    ps.setString(3, request.getTitle());
                    ps.setString(4, request.getContent());
                    ps.setString(5, request.getEvaluation().getValue());
                    ps.setString(6, request.getDate());
                    ps.setString(7, request.getMemoType().getValue());
                    if (actualId != null) {
                        ps.setLong(8, actualId);
                    } else {
                        ps.setNull(8, java.sql.Types.BIGINT);
                    }
                    if (targetId != null) {
                        ps.setLong(9, targetId);
                    } else {
                        ps.setNull(9, java.sql.Types.BIGINT);
                    }
                    return ps;
                },
                keyHolder);

        return keyHolder.getKey().longValue();
    }

    public void updateByPublicId(
            JdbcTemplate jdbcTemplate,
            String publicId,
            MemoCreateRequest request,
            Long actualId,
            Long targetId) {
        String sql =
                String.format(
                        "UPDATE %s SET %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = datetime('now') WHERE %s = ? AND %s IS NULL",
                        SqliteSchema.TABLE_MEMO,
                        SqliteSchema.COL_IMPORTANCE,
                        SqliteSchema.COL_TITLE,
                        SqliteSchema.COL_CONTENT,
                        SqliteSchema.COL_EVALUATION,
                        SqliteSchema.COL_DATE,
                        SqliteSchema.COL_MEMO_TYPE,
                        SqliteSchema.COL_ACTUAL_ID,
                        SqliteSchema.COL_TARGET_ID,
                        SqliteSchema.COL_UPDATED_AT,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_DELETED_AT);

        jdbcTemplate.update(
                sql,
                ps -> {
                    ps.setString(1, request.getImportance().getValue());
                    ps.setString(2, request.getTitle());
                    ps.setString(3, request.getContent());
                    ps.setString(4, request.getEvaluation().getValue());
                    ps.setString(5, request.getDate());
                    ps.setString(6, request.getMemoType().getValue());
                    if (actualId != null) {
                        ps.setLong(7, actualId);
                    } else {
                        ps.setNull(7, java.sql.Types.BIGINT);
                    }
                    if (targetId != null) {
                        ps.setLong(8, targetId);
                    } else {
                        ps.setNull(8, java.sql.Types.BIGINT);
                    }
                    ps.setString(9, publicId);
                });
    }

    public void deleteByPublicId(JdbcTemplate jdbcTemplate, String publicId) {
        String sql =
                String.format(
                        "UPDATE %s SET %s = datetime('now') WHERE %s = ? AND %s IS NULL",
                        SqliteSchema.TABLE_MEMO,
                        SqliteSchema.COL_DELETED_AT,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_DELETED_AT);

        jdbcTemplate.update(sql, ps -> ps.setString(1, publicId));
    }
}
