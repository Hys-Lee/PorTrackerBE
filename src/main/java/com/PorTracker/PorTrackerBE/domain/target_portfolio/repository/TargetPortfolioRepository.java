package com.PorTracker.PorTrackerBE.domain.target_portfolio.repository;

import com.PorTracker.PorTrackerBE.domain.target_portfolio.entity.TargetPortfolioRecord;
import com.PorTracker.PorTrackerBE.global.constant.SqliteSchema;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TargetPortfolioRepository {

    private static final String BASE_SELECT_SQL =
            String.format(
                    " SELECT %s, %s, %s, %s, %s, %s FROM %s",
                    SqliteSchema.COL_ID,
                    SqliteSchema.COL_PUBLIC_ID,
                    SqliteSchema.COL_NAME,
                    SqliteSchema.COL_DATE,
                    SqliteSchema.COL_CREATED_AT,
                    SqliteSchema.COL_DELETED_AT,
                    SqliteSchema.TABLE_TARGET_PORTFOLIO);

    private final RowMapper<TargetPortfolioRecord> portfolioMapper =
            (rs, rowNum) ->
                    TargetPortfolioRecord.builder()
                            .id(rs.getLong(SqliteSchema.COL_ID))
                            .publicId(rs.getString(SqliteSchema.COL_PUBLIC_ID))
                            .name(rs.getString(SqliteSchema.COL_NAME))
                            .date(rs.getString(SqliteSchema.COL_DATE))
                            .createdAt(rs.getString(SqliteSchema.COL_CREATED_AT))
                            .deletedAt(rs.getString(SqliteSchema.COL_DELETED_AT))
                            .build();

    public List<TargetPortfolioRecord> findAll(JdbcTemplate jdbcTemplate) {
        String sql = BASE_SELECT_SQL + " WHERE " + SqliteSchema.COL_DELETED_AT + " IS NULL";
        return jdbcTemplate.query(sql, portfolioMapper);
    }

    public Optional<TargetPortfolioRecord> findByPublicId(
            JdbcTemplate jdbcTemplate, String publicId) {
        String sql =
                BASE_SELECT_SQL
                        + " WHERE "
                        + SqliteSchema.COL_PUBLIC_ID
                        + "=? AND "
                        + SqliteSchema.COL_DELETED_AT
                        + " IS NULL";
        return jdbcTemplate.query(sql, ps -> ps.setString(1, publicId), portfolioMapper).stream()
                .findFirst();
    }

    public Long save(JdbcTemplate jdbcTemplate, String name, String date, String publicId) {
        String sql =
                String.format(
                        "INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?)",
                        SqliteSchema.TABLE_TARGET_PORTFOLIO,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_NAME,
                        SqliteSchema.COL_DATE);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        // jdbcTemplate.update(sql, ps -> {
        // ps.setString(1, publicId);
        // ps.setString(2, name);
        // ps.setString(3, date);
        // });
        jdbcTemplate.update(
                conn -> {
                    PreparedStatement ps = conn.prepareStatement(sql, new String[] {"id"});

                    ps.setString(1, publicId);
                    ps.setString(2, name);
                    ps.setString(3, date);
                    return ps;
                },
                keyHolder);

        return keyHolder.getKey().longValue();

        // return jdbcTemplate.queryForObject("SELECT last_insert_rowid()", Long.class);
    }

    public void updateByPublicId(
            JdbcTemplate jdbcTemplate, String publicId, String name, String date) {
        String sql =
                String.format(
                        "UPDATE %s SET %s = ?, %s = ?, %s = datetime('now') WHERE %s = ? AND %s IS NULL",
                        SqliteSchema.TABLE_TARGET_PORTFOLIO,
                        SqliteSchema.COL_NAME,
                        SqliteSchema.COL_DATE,
                        SqliteSchema.COL_UPDATED_AT,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_DELETED_AT);

        jdbcTemplate.update(
                sql,
                ps -> {
                    ps.setString(1, name);
                    ps.setString(2, date);
                    ps.setString(3, publicId);
                });
    }

    public void deleteByPublicId(JdbcTemplate jdbcTemplate, String publicId) {
        String sql =
                String.format(
                        "UPDATE %s SET %s = datetime('now') WHERE %s = ? AND %s IS NULL",
                        SqliteSchema.TABLE_TARGET_PORTFOLIO,
                        SqliteSchema.COL_DELETED_AT,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_DELETED_AT);

        jdbcTemplate.update(sql, ps -> ps.setString(1, publicId));
    }
}
