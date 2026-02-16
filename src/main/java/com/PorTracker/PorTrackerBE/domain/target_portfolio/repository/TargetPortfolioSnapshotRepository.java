package com.PorTracker.PorTrackerBE.domain.target_portfolio.repository;

import com.PorTracker.PorTrackerBE.domain.target_portfolio.entity.TargetPortfolioSnapshotRecord;
import com.PorTracker.PorTrackerBE.global.constant.SqliteSchema;
import java.sql.PreparedStatement;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TargetPortfolioSnapshotRepository {

    public Long save(JdbcTemplate jdbcTemplate, Long portfolioId) {
        String sql =
                String.format(
                        "INSERT INTO %s (%s) VALUES (?)",
                        SqliteSchema.TABLE_TARGET_PORTFOLIO_SNAPSHOT,
                        SqliteSchema.COL_PORTFOLIO_ID);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        // jdbcTemplate.update(sql, ps -> ps.setLong(1, portfolioId));
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
                    ps.setLong(1, portfolioId);
                    return ps;
                },
                keyHolder);
        return keyHolder.getKey().longValue();
        // return jdbcTemplate.queryForObject("SELECT last_insert_rowid()", Long.class);
    }

    public Optional<TargetPortfolioSnapshotRecord> findLatestByPortfolioId(
            JdbcTemplate jdbcTemplate, Long portfolioId) {
        String sql =
                String.format(
                        "SELECT %s, %s, %s FROM %s WHERE %s = ? ORDER BY %s DESC LIMIT 1",
                        SqliteSchema.COL_ID,
                        SqliteSchema.COL_PORTFOLIO_ID,
                        SqliteSchema.COL_CREATED_AT,
                        SqliteSchema.TABLE_TARGET_PORTFOLIO_SNAPSHOT,
                        SqliteSchema.COL_PORTFOLIO_ID,
                        SqliteSchema.COL_ID);

        return jdbcTemplate
                .query(
                        sql,
                        ps -> ps.setLong(1, portfolioId),
                        (rs, rowNum) ->
                                TargetPortfolioSnapshotRecord.builder()
                                        .id(rs.getLong(SqliteSchema.COL_ID))
                                        .portfolioId(rs.getLong(SqliteSchema.COL_PORTFOLIO_ID))
                                        .createdAt(rs.getString(SqliteSchema.COL_CREATED_AT))
                                        .build())
                .stream()
                .findFirst();
    }
}
