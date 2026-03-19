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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MemoRepository {

    private static final String actualPortfolioPublicIdName = "actual_portfolio_public_id";
    private static final String targetPortfolioPublicIdName = "target_portfolio_public_id";

    private static final String CORE_SELECT_SQL =
            String.format(
                    "SELECT m.%s, m.%s, m.%s, m.%s, m.%s, m.%s, m.%s, m.%s, m.%s, m.%s, m.%s",
                    SqliteSchema.COL_ID,
                    SqliteSchema.COL_PUBLIC_ID,
                    SqliteSchema.COL_CREATED_AT,
                    SqliteSchema.COL_IMPORTANCE,
                    SqliteSchema.COL_TITLE,
                    SqliteSchema.COL_CONTENT,
                    SqliteSchema.COL_EVALUATION,
                    SqliteSchema.COL_DATE,
                    SqliteSchema.COL_MEMO_TYPE,
                    SqliteSchema.COL_ACTUAL_ID,
                    SqliteSchema.COL_TARGET_ID);

    private static final String BASE_SELECT_SQL =
            String.format(
                    CORE_SELECT_SQL
                            + ", ap.%s as %s, tp.%s as %s"
                            + " FROM %s m LEFT JOIN %s ap ON m.%s=ap.%s LEFT JOIN %s tp ON m.%s=tp.%s",
                    // select
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

    private static final String BULK_SELECT_SQL =
            String.format(
                    CORE_SELECT_SQL + " FROM %s m WHERE m.%s IN (:bulkIds) AND m.%s IS NULL",
                    SqliteSchema.TABLE_MEMO,
                    SqliteSchema.COL_PUBLIC_ID,
                    SqliteSchema.COL_DELETED_AT);

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
                    ps.setString(6, request.getDate().toString());
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
                    ps.setString(5, request.getDate().toString());
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

    public void patchIdsByPublicId(
            JdbcTemplate jdbcTemplate, String publicId, Long actualId, Long targetId) {
        String sql =
                String.format(
                        "UPDATE %s SET %s = ?, %s = ?, %s = datetime('now') WHERE %s = ? AND %s IS NULL",
                        SqliteSchema.TABLE_MEMO,
                        SqliteSchema.COL_ACTUAL_ID,
                        SqliteSchema.COL_TARGET_ID,
                        SqliteSchema.COL_UPDATED_AT,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_DELETED_AT);

        jdbcTemplate.update(
                sql,
                ps -> {
                    if (actualId != null) {
                        ps.setLong(1, actualId);
                    } else {
                        ps.setNull(1, java.sql.Types.BIGINT);
                    }
                    if (targetId != null) {
                        ps.setLong(2, targetId);
                    } else {
                        ps.setNull(2, java.sql.Types.BIGINT);
                    }
                    ps.setString(3, publicId);
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

    public void nullifyActualId(JdbcTemplate jdbcTemplate, Long actualId) {
        String sql =
                String.format(
                        "UPDATE %s SET %s = NULL, %s = datetime('now') WHERE %s = ? AND %s IS NULL",
                        SqliteSchema.TABLE_MEMO,
                        SqliteSchema.COL_ACTUAL_ID,
                        SqliteSchema.COL_UPDATED_AT,
                        SqliteSchema.COL_ACTUAL_ID,
                        SqliteSchema.COL_DELETED_AT);
        jdbcTemplate.update(sql, ps -> ps.setLong(1, actualId));
    }

    public void nullifyTargetId(JdbcTemplate jdbcTemplate, Long targetId) {
        String sql =
                String.format(
                        "UPDATE %s SET %s = NULL, %s = datetime('now') WHERE %s = ? AND %s IS NULL",
                        SqliteSchema.TABLE_MEMO,
                        SqliteSchema.COL_TARGET_ID,
                        SqliteSchema.COL_UPDATED_AT,
                        SqliteSchema.COL_TARGET_ID,
                        SqliteSchema.COL_DELETED_AT);
        jdbcTemplate.update(sql, ps -> ps.setLong(1, targetId));
    }

    public List<MemoRecord> findByPublicIds(
            NamedParameterJdbcTemplate jdbcTemplate, List<String> publicIds) {
        if (publicIds == null || publicIds.isEmpty()) return List.of();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("bulkIds", publicIds);

        return jdbcTemplate.query(BULK_SELECT_SQL, parameters, memoMapper);
    }

    public List<MemoRecord> search(
            NamedParameterJdbcTemplate jdbcTemplate,
            com.PorTracker.PorTrackerBE.domain.memo.dto.MemoSearchRequest request) {
        StringBuilder sql = new StringBuilder(BASE_SELECT_SQL);
        sql.append(String.format(" WHERE m.%s IS NULL", SqliteSchema.COL_DELETED_AT));

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (request.getImportances() != null && !request.getImportances().isEmpty()) {
            String paramName = "importance";
            sql.append(String.format(" AND m.%s IN (:%s)", SqliteSchema.COL_IMPORTANCE, paramName));
            params.addValue(
                    paramName,
                    request.getImportances().stream()
                            .map(
                                    com.PorTracker.PorTrackerBE.domain.memo.entity.Importance
                                            ::getValue)
                            .toList());
        }
        if (request.getTitles() != null && !request.getTitles().isEmpty()) {
            sql.append(" AND (");
            for (int i = 0; i < request.getTitles().size(); i++) {
                String paramName = "title" + i;
                sql.append(i == 0 ? "" : " OR ")
                        .append(String.format("m.%s LIKE :%s", SqliteSchema.COL_TITLE, paramName));
                params.addValue(paramName, "%" + request.getTitles().get(i) + "%");
            }
            sql.append(")");
        }
        if (request.getEvaluations() != null && !request.getEvaluations().isEmpty()) {
            String paramName = "evaluation";
            sql.append(String.format(" AND m.%s IN (:%s)", SqliteSchema.COL_EVALUATION, paramName));
            params.addValue(
                    paramName,
                    request.getEvaluations().stream()
                            .map(
                                    com.PorTracker.PorTrackerBE.domain.memo.entity.Evaluation
                                            ::getValue)
                            .toList());
        }
        if (request.getMemoTypes() != null && !request.getMemoTypes().isEmpty()) {
            String paramName = "memoType";
            sql.append(String.format(" AND m.%s IN (:%s)", SqliteSchema.COL_MEMO_TYPE, paramName));
            params.addValue(
                    paramName,
                    request.getMemoTypes().stream()
                            .map(com.PorTracker.PorTrackerBE.domain.memo.entity.MemoType::getValue)
                            .toList());
        }
        if (request.getActualIds() != null && !request.getActualIds().isEmpty()) {
            String paramName = "actualId";
            sql.append(String.format(" AND ap.%s IN (:%s)", SqliteSchema.COL_PUBLIC_ID, paramName));
            params.addValue(paramName, request.getActualIds());
        }
        if (request.getTargetIds() != null && !request.getTargetIds().isEmpty()) {
            String paramName = "targetId";
            sql.append(String.format(" AND tp.%s IN (:%s)", SqliteSchema.COL_PUBLIC_ID, paramName));
            params.addValue(paramName, request.getTargetIds());
        }
        if (request.getStartDate() != null) {
            String paramName = "startDate";
            sql.append(String.format(" AND m.%s >= :%s", SqliteSchema.COL_DATE, paramName));
            params.addValue(paramName, request.getStartDate());
        }
        if (request.getEndDate() != null) {
            String paramName = "endDate";
            sql.append(String.format(" AND m.%s <= :%s", SqliteSchema.COL_DATE, paramName));
            params.addValue(paramName, request.getEndDate());
        }

        sql.append(
                String.format(
                        " ORDER BY m.%s DESC, m.%s DESC",
                        SqliteSchema.COL_DATE, SqliteSchema.COL_CREATED_AT));

        String limitParam = "limit";
        String offsetParam = "offset";
        sql.append(String.format(" LIMIT :%s OFFSET :%s", limitParam, offsetParam));
        params.addValue(limitParam, request.getLimit());
        params.addValue(offsetParam, request.getOffset());

        return jdbcTemplate.query(sql.toString(), params, memoMapper);
    }

    public List<MemoRecord> findRecentByAssetId(
            NamedParameterJdbcTemplate jdbcTemplate, Long assetId, int limit) {
        String sql =
                BASE_SELECT_SQL
                        + " WHERE ap."
                        + SqliteSchema.COL_ASSET_ID
                        + " = :assetId"
                        + " AND m."
                        + SqliteSchema.COL_DELETED_AT
                        + " IS NULL"
                        + " ORDER BY m."
                        + SqliteSchema.COL_DATE
                        + " DESC, m."
                        + SqliteSchema.COL_CREATED_AT
                        + " DESC"
                        + " LIMIT :limit";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("assetId", assetId);
        params.addValue("limit", limit);

        return jdbcTemplate.query(sql, params, memoMapper);
    }

    public List<MemoRecord> enrichWithTags(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate, List<MemoRecord> memos) {
        if (memos == null || memos.isEmpty()) return memos;

        List<Long> memoIds = memos.stream().map(MemoRecord::getId).toList();

        String sql =
                "SELECT mt.memo_id, t.content FROM memo_tag mt JOIN tag t ON mt.tag_id = t.id WHERE mt.memo_id IN (:memoIds) AND mt.deleted_at IS NULL";

        MapSqlParameterSource params = new MapSqlParameterSource("memoIds", memoIds);

        java.util.Map<Long, List<String>> memoTagsMap = new java.util.HashMap<>();
        namedParameterJdbcTemplate.query(
                sql,
                params,
                rs -> {
                    Long memoId = rs.getLong("memo_id");
                    String tagContent = rs.getString("content");
                    memoTagsMap
                            .computeIfAbsent(memoId, k -> new java.util.ArrayList<>())
                            .add(tagContent);
                });

        return memos.stream()
                .map(
                        memo -> {
                            List<String> tags =
                                    memoTagsMap.getOrDefault(
                                            memo.getId(), new java.util.ArrayList<>());
                            return memo.toBuilder().tags(tags).build();
                        })
                .toList();
    }

    public void updateTagsByMemoId(JdbcTemplate jdbcTemplate, Long memoId, List<Long> tagIds) {
        jdbcTemplate.update(
                "UPDATE memo_tag SET deleted_at = datetime('now') WHERE memo_id = ? AND deleted_at IS NULL",
                memoId);

        if (tagIds == null || tagIds.isEmpty()) return;

        String sql =
                "INSERT INTO memo_tag (memo_id, tag_id, deleted_at) "
                        + "VALUES (?, ?, NULL) "
                        + "ON CONFLICT(memo_id, tag_id) DO UPDATE SET deleted_at = NULL, updated_at = datetime('now')";

        jdbcTemplate.batchUpdate(
                sql,
                new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(java.sql.PreparedStatement ps, int i)
                            throws java.sql.SQLException {
                        ps.setLong(1, memoId);
                        ps.setLong(2, tagIds.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return tagIds.size();
                    }
                });
    }
}
