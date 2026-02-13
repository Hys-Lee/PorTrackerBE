
package com.PorTracker.PorTrackerBE.domain.asset.service;

import com.PorTracker.PorTrackerBE.domain.asset.dto.AssetTypeRequest;
import com.PorTracker.PorTrackerBE.domain.asset.entity.AssetTypeRecord;
import com.PorTracker.PorTrackerBE.global.constant.SqliteSchema;
import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import com.PorTracker.PorTrackerBE.service.sqlite.SqliteDatabaseManager;
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
        public List<AssetTypeRecord> getAllAssetTypes(String userId) {
                JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

                String sql = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s",
                                SqliteSchema.COL_ID, SqliteSchema.COL_PUBLIC_ID,
                                SqliteSchema.COL_CREATED_AT, SqliteSchema.COL_UPDATED_AT,
                                SqliteSchema.COL_DELETED_AT, SqliteSchema.COL_NAME,
                                SqliteSchema.TABLE_ASSET_TYPE);

                return jdbcTemplate.query(sql, (rs, rowNum) -> AssetTypeRecord.builder()
                                .id(rs.getLong(SqliteSchema.COL_ID))
                                .publicId(rs.getString(SqliteSchema.COL_PUBLIC_ID))
                                .createdAt(rs.getString(SqliteSchema.COL_CREATED_AT))
                                .updatedAt(rs.getString(SqliteSchema.COL_UPDATED_AT))
                                .deletedAt(rs.getString(SqliteSchema.COL_DELETED_AT))
                                .name(rs.getString(SqliteSchema.COL_NAME)).build());
        }

        // NPE 버그 수정된 메서드
        public AssetTypeRecord getAssetTypeIdByPublicId(String userId, String publicId) {
                JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

                // [DEBUG] 현재 이 유저의 DB에 어떤 데이터가 있는지 먼저 확인
                Integer totalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM asset_type",
                                Integer.class);
                log.info("현재 유저({})의 asset_type 총 개수: {}", userId, totalCount);

                String sql = String.format("SELECT %s,%s,%s,%s,%s,%s FROM %s WHERE %s = ?",
                                SqliteSchema.COL_ID, SqliteSchema.COL_PUBLIC_ID,
                                SqliteSchema.COL_CREATED_AT, SqliteSchema.COL_UPDATED_AT,
                                SqliteSchema.COL_DELETED_AT, SqliteSchema.COL_NAME,
                                SqliteSchema.TABLE_ASSET_TYPE, SqliteSchema.COL_PUBLIC_ID);
                log.info("실행 쿼리: {} | 파라미터: [{}]", sql, publicId);
                try {
                        List<AssetTypeRecord> results = jdbcTemplate.query(sql, ps -> {
                                ps.setString(1, publicId);
                        }, (rs, rowNum) -> AssetTypeRecord.builder()
                                        .id(rs.getLong(SqliteSchema.COL_ID))
                                        .publicId(rs.getString(SqliteSchema.COL_PUBLIC_ID))
                                        .createdAt(rs.getString(SqliteSchema.COL_CREATED_AT))
                                        .updatedAt(rs.getString(SqliteSchema.COL_UPDATED_AT))
                                        .deletedAt(rs.getString(SqliteSchema.COL_DELETED_AT))
                                        .name(rs.getString(SqliteSchema.COL_NAME)).build());

                        if (results.isEmpty())
                                throw new EmptyResultDataAccessException(1);
                        return results.get(0);

                } catch (EmptyResultDataAccessException e) {
                        throw new BusinessException(ErrorCode.NO_DATA);
                }
        }

        // NPE 버그 수정된 메서드
        public void addAssetType(String userId, AssetTypeRequest request) {
                JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

                String sql = String.format("INSERT OR IGNORE INTO %s (%s, %s) VALUES (?, ?)",
                                SqliteSchema.TABLE_ASSET_TYPE, SqliteSchema.COL_PUBLIC_ID,
                                SqliteSchema.COL_NAME);

                String publicId = UUID.randomUUID().toString();

                jdbcTemplate.update(sql, ps -> {
                        ps.setString(1, publicId);
                        ps.setString(2, request.getName());
                });

                log.info("asset type recorded successfully for user: {}", userId);
        }
}
