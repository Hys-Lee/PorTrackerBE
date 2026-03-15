package com.PorTracker.PorTrackerBE.domain.target_portfolio.repository;

import com.PorTracker.PorTrackerBE.domain.target_portfolio.entity.TargetPortfolioRecord;
import com.PorTracker.PorTrackerBE.global.constant.SqliteSchema;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TargetPortfolioRepository {

    private static final String CORE_SELECT_SQL =
            String.format(
                    "SELECT %s, %s, %s, %s, %s, %s",
                    SqliteSchema.COL_ID,
                    SqliteSchema.COL_PUBLIC_ID,
                    SqliteSchema.COL_NAME,
                    SqliteSchema.COL_DATE,
                    SqliteSchema.COL_CREATED_AT,
                    SqliteSchema.COL_DELETED_AT);

    private static final String BASE_SELECT_SQL =
            String.format(CORE_SELECT_SQL + " FROM %s", SqliteSchema.TABLE_TARGET_PORTFOLIO);

    private static final String BULK_SELECT_SQL =
            String.format(
                    CORE_SELECT_SQL + " FROM %s WHERE %s IN (:bulkIds) AND %s IS NULL",
                    SqliteSchema.TABLE_TARGET_PORTFOLIO,
                    SqliteSchema.COL_PUBLIC_ID,
                    SqliteSchema.COL_DELETED_AT);

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

    public List<TargetPortfolioRecord> findByPublicIds(
            NamedParameterJdbcTemplate jdbcTemplate, List<String> publicIds) {
        if (publicIds == null || publicIds.isEmpty()) return List.of();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("bulkIds", publicIds);

        return jdbcTemplate.query(BULK_SELECT_SQL, parameters, portfolioMapper);
    }

    public List<TargetPortfolioRecord> search(
            NamedParameterJdbcTemplate jdbcTemplate,
            com.PorTracker.PorTrackerBE.domain.target_portfolio.dto.TargetPortfolioSearchRequest
                    request) {
        StringBuilder sql = new StringBuilder(BASE_SELECT_SQL);
        sql.append(String.format(" WHERE %s IS NULL", SqliteSchema.COL_DELETED_AT));

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (request.getName() != null && !request.getName().isEmpty()) {
            sql.append(" AND (");
            for (int i = 0; i < request.getName().size(); i++) {
                String paramName = "name" + i;
                sql.append(i == 0 ? "" : " OR ").append(String.format("%s LIKE :%s", SqliteSchema.COL_NAME, paramName));
                params.addValue(paramName, "%" + request.getName().get(i) + "%");
            }
            sql.append(")");
        }

        if (request.getStartDate() != null) {
            String paramName = "startDate";
            sql.append(String.format(" AND %s >= :%s", SqliteSchema.COL_DATE, paramName));
            params.addValue(paramName, request.getStartDate());
        }

        if (request.getEndDate() != null) {
            String paramName = "endDate";
            sql.append(String.format(" AND %s <= :%s", SqliteSchema.COL_DATE, paramName));
            params.addValue(paramName, request.getEndDate());
        }

        sql.append(
                String.format(
                        " ORDER BY %s DESC, %s DESC",
                        SqliteSchema.COL_DATE, SqliteSchema.COL_CREATED_AT));

        String limitParam = "limit";
        String offsetParam = "offset";
        sql.append(String.format(" LIMIT :%s OFFSET :%s", limitParam, offsetParam));
        params.addValue(limitParam, request.getLimit());
        params.addValue(offsetParam, request.getOffset());

        return jdbcTemplate.query(sql.toString(), params, portfolioMapper);
    }
}
