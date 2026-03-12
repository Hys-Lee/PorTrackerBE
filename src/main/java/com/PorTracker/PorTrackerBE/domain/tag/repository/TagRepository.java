package com.PorTracker.PorTrackerBE.domain.tag.repository;

import com.PorTracker.PorTrackerBE.domain.tag.dto.TagCreateRequest;
import com.PorTracker.PorTrackerBE.domain.tag.entity.TagRecord;
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
public class TagRepository {

    private final RowMapper<TagRecord> tagMapper =
            (rs, rowNum) ->
                    TagRecord.builder()
                            .id(rs.getLong(SqliteSchema.COL_ID))
                            .publicId(rs.getString(SqliteSchema.COL_PUBLIC_ID))
                            .content(rs.getString(SqliteSchema.COL_CONTENT))
                            .build();

    public List<TagRecord> findAll(JdbcTemplate jdbcTemplate) {
        String sql = String.format("SELECT * FROM %s", SqliteSchema.TABLE_TAG);
        return jdbcTemplate.query(sql, tagMapper);
    }

    public Optional<TagRecord> findByPublicId(JdbcTemplate jdbcTemplate, String publicId) {
        String sql =
                String.format(
                        "SELECT * FROM %s WHERE %s = ?",
                        SqliteSchema.TABLE_TAG, SqliteSchema.COL_PUBLIC_ID);
        return jdbcTemplate.query(sql, ps -> ps.setString(1, publicId), tagMapper).stream()
                .findFirst();
    }

    public List<TagRecord> findByPublicIds(
            org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate jdbcTemplate,
            List<String> publicIds) {
        if (publicIds == null || publicIds.isEmpty()) return List.of();

        String sql =
                String.format(
                        "SELECT * FROM %s WHERE %s IN (:bulkIds)",
                        SqliteSchema.TABLE_TAG, SqliteSchema.COL_PUBLIC_ID);
        org.springframework.jdbc.core.namedparam.MapSqlParameterSource parameters =
                new org.springframework.jdbc.core.namedparam.MapSqlParameterSource();
        parameters.addValue("bulkIds", publicIds);

        return jdbcTemplate.query(sql, parameters, tagMapper);
    }

    public Optional<TagRecord> findByContent(JdbcTemplate jdbcTemplate, String content) {
        String sql =
                String.format(
                        "SELECT * FROM %s WHERE %s = ?",
                        SqliteSchema.TABLE_TAG, SqliteSchema.COL_CONTENT);
        return jdbcTemplate.query(sql, ps -> ps.setString(1, content), tagMapper).stream()
                .findFirst();
    }

    public Long save(JdbcTemplate jdbcTemplate, TagCreateRequest request, String publicId) {
        String sql =
                String.format(
                        "INSERT INTO %s (%s, %s) VALUES (?, ?)",
                        SqliteSchema.TABLE_TAG,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_CONTENT);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                conn -> {
                    PreparedStatement ps = conn.prepareStatement(sql, new String[] {"id"});
                    ps.setString(1, publicId);
                    ps.setString(2, request.getContent());
                    return ps;
                },
                keyHolder);

        return keyHolder.getKey().longValue();
    }

    public void updateByPublicId(
            JdbcTemplate jdbcTemplate, String publicId, TagCreateRequest request) {
        String sql =
                String.format(
                        "UPDATE %s SET %s = ? WHERE %s = ?",
                        SqliteSchema.TABLE_TAG,
                        SqliteSchema.COL_CONTENT,
                        SqliteSchema.COL_PUBLIC_ID);
        jdbcTemplate.update(sql, request.getContent(), publicId);
    }

    public void deleteByPublicId(JdbcTemplate jdbcTemplate, String publicId) {
        String sql =
                String.format(
                        "DELETE FROM %s WHERE %s = ?",
                        SqliteSchema.TABLE_TAG, SqliteSchema.COL_PUBLIC_ID);
        jdbcTemplate.update(sql, publicId);
    }

    /**
     * 태그 content 리스트를 받아서 upsert 후 tag id 리스트를 반환. content가 이미 존재하면 기존 tag id, 없으면 새로 생성한 tag id를
     * 반환.
     */
    public List<Long> upsertTagsByContent(JdbcTemplate jdbcTemplate, List<String> tagContents) {
        if (tagContents == null || tagContents.isEmpty()) return List.of();

        // List<Long> tagIds = new java.util.ArrayList<>();

        // for (String content : tagContents) {
        //     String trimmed = content.trim();
        //     if (trimmed.isEmpty()) continue;

        //     // 1. 기존 태그 검색
        //     Optional<TagRecord> existing = findByContent(jdbcTemplate, trimmed);

        //     if (existing.isPresent()) {
        //         tagIds.add(existing.get().getId());
        //     } else {
        //         // 2. 없으면 새로 생성
        //         String publicId = java.util.UUID.randomUUID().toString();
        //         TagCreateRequest createReq = new TagCreateRequest(trimmed);
        //         Long newId = save(jdbcTemplate, createReq, publicId);
        //         tagIds.add(newId);
        //     }
        // }
        // return tagIds;

        // 배치 처리하기
        String insertSql = String.format("INSERT OR IGNORE INTO %s (%s, %s) VALUES (?, ?)", SqliteSchema.TABLE_TAG, SqliteSchema.COL_PUBLIC_ID, SqliteSchema.COL_CONTENT);
        jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter(){
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    String content = tagContents.get(i);
                    String trimmed = content.trim();
                    if (trimmed.isEmpty()) return;
                    ps.setString(1, java.util.UUID.randomUUID().toString());
                    ps.setString(2, trimmed);
                }
                @Override
                public int getBatchSize() {
                    return tagContents.size();
                }
        })

        // 관련 내부 id들 배칭 조회
        String inSql = tagContents.stream().map(content -> "?").collect(Collectors.joining(","));
        String selectSql = String.format("SELECT %s, %s FROM %s WHERE %s IN (%s)", SqliteSchema.COL_ID, SqliteSchema.TABLE_TAG, SqliteSchema.COL_CONTENT, inSql);
        
        return jdbcTemplate.queryForList(selectSql, Long.class, tagContents.toArray());
        


        
    }
}
