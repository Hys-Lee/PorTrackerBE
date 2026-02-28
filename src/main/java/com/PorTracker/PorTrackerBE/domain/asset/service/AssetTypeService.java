package com.PorTracker.PorTrackerBE.domain.asset.service;

import com.PorTracker.PorTrackerBE.domain.asset.dto.AssetTypeRequest;
import com.PorTracker.PorTrackerBE.domain.asset.entity.AssetTypeRecord;
import com.PorTracker.PorTrackerBE.global.common.UserContextHolder;
import com.PorTracker.PorTrackerBE.global.constant.SqliteSchema;
import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import com.PorTracker.PorTrackerBE.global.infra.sqlite.SqliteDatabaseManager;

// import com.PorTracker.PorTrackerBE.service.sqlite.SqliteDatabaseManager;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetTypeService {
    private final SqliteDatabaseManager sqliteManager;

    // 기존 메서드 유지
    // public List<AssetTypeRecord> getAllAssetTypes(String userId) {
    public List<AssetTypeRecord> getAllAssetTypes() {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        String sql =
                String.format(
                        "SELECT %s, %s, %s, %s, %s, %s FROM %s",
                        SqliteSchema.COL_ID,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_CREATED_AT,
                        SqliteSchema.COL_UPDATED_AT,
                        SqliteSchema.COL_DELETED_AT,
                        SqliteSchema.COL_NAME,
                        SqliteSchema.TABLE_ASSET_TYPE);

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        AssetTypeRecord.builder()
                                .id(rs.getLong(SqliteSchema.COL_ID))
                                .publicId(rs.getString(SqliteSchema.COL_PUBLIC_ID))
                                .createdAt(rs.getString(SqliteSchema.COL_CREATED_AT))
                                .updatedAt(rs.getString(SqliteSchema.COL_UPDATED_AT))
                                .deletedAt(rs.getString(SqliteSchema.COL_DELETED_AT))
                                .name(rs.getString(SqliteSchema.COL_NAME))
                                .build());
    }

    // NPE 버그 수정된 메서드
    // public AssetTypeRecord getAssetTypeIdByPublicId(String userId, String publicId) {
    public AssetTypeRecord getAssetTypeIdByPublicId(String publicId) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        // [DEBUG] 현재 이 유저의 DB에 어떤 데이터가 있는지 먼저 확인
        Integer totalCount =
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM asset_type", Integer.class);
        log.info("현재 유저({})의 asset_type 총 개수: {}", userId, totalCount);

        String sql =
                String.format(
                        "SELECT %s,%s,%s,%s,%s,%s FROM %s WHERE %s = ?",
                        SqliteSchema.COL_ID,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_CREATED_AT,
                        SqliteSchema.COL_UPDATED_AT,
                        SqliteSchema.COL_DELETED_AT,
                        SqliteSchema.COL_NAME,
                        SqliteSchema.TABLE_ASSET_TYPE,
                        SqliteSchema.COL_PUBLIC_ID);
        log.info("실행 쿼리: {} | 파라미터: [{}]", sql, publicId);
        try {
            List<AssetTypeRecord> results =
                    jdbcTemplate.query(
                            sql,
                            ps -> {
                                ps.setString(1, publicId);
                            },
                            (rs, rowNum) ->
                                    AssetTypeRecord.builder()
                                            .id(rs.getLong(SqliteSchema.COL_ID))
                                            .publicId(rs.getString(SqliteSchema.COL_PUBLIC_ID))
                                            .createdAt(rs.getString(SqliteSchema.COL_CREATED_AT))
                                            .updatedAt(rs.getString(SqliteSchema.COL_UPDATED_AT))
                                            .deletedAt(rs.getString(SqliteSchema.COL_DELETED_AT))
                                            .name(rs.getString(SqliteSchema.COL_NAME))
                                            .build());

            if (results.isEmpty()) throw new EmptyResultDataAccessException(1);
            return results.get(0);

        } catch (EmptyResultDataAccessException e) {
            throw new BusinessException(ErrorCode.NO_DATA, "asset-types");
        }
    }

    // NPE 버그 수정된 메서드
    // public void addAssetType(String userId, AssetTypeRequest request) {
    public void addAssetType(AssetTypeRequest request) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        String sql =
                String.format(
                        "INSERT OR IGNORE INTO %s (%s, %s) VALUES (?, ?)",
                        SqliteSchema.TABLE_ASSET_TYPE,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_NAME);

        String publicId = UUID.randomUUID().toString();

        jdbcTemplate.update(
                sql,
                ps -> {
                    ps.setString(1, publicId);
                    ps.setString(2, request.getName());
                });

        log.info("asset type recorded successfully for user: {}", userId);
    }

    // public void updateAssetType(String userId, String publicId, AssetTypeRequest request) {
    public void updateAssetType(String publicId, AssetTypeRequest request) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        String sql =
                String.format(
                        "UPDATE %s SET %s = ?, %s = datetime('now') WHERE %s = ? AND %s IS NULL",
                        SqliteSchema.TABLE_ASSET_TYPE,
                        SqliteSchema.COL_NAME,
                        SqliteSchema.COL_UPDATED_AT,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_DELETED_AT);

        int updated =
                jdbcTemplate.update(
                        sql,
                        ps -> {
                            ps.setString(1, request.getName());
                            ps.setString(2, publicId);
                        });

        if (updated == 0) {
            throw new BusinessException(ErrorCode.NO_DATA, "asset-types");
        }

        log.info("asset type updated successfully for user: {}, publicId: {}", userId, publicId);
    }

    // public void deleteAssetType(String userId, String publicId) {
    public void deleteAssetType(String publicId) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        String sql =
                String.format(
                        "UPDATE %s SET %s = datetime('now') WHERE %s = ? AND %s IS NULL",
                        SqliteSchema.TABLE_ASSET_TYPE,
                        SqliteSchema.COL_DELETED_AT,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_DELETED_AT);

        int deleted = jdbcTemplate.update(sql, ps -> ps.setString(1, publicId));

        if (deleted == 0) {
            throw new BusinessException(ErrorCode.NO_DATA, "asset-types");
        }

        log.info("asset type deleted successfully for user: {}, publicId: {}", userId, publicId);
    }
}
