
package com.PorTracker.PorTrackerBE.domain.asset.service;

import com.PorTracker.PorTrackerBE.domain.asset.dto.AssetCreateRequest;
import com.PorTracker.PorTrackerBE.domain.asset.entity.AssetRecord;
import com.PorTracker.PorTrackerBE.domain.asset.entity.AssetTypeRecord;
import com.PorTracker.PorTrackerBE.domain.currency.entity.CurrencyTypeRecord;
import com.PorTracker.PorTrackerBE.domain.currency.service.CurrencyService;
import com.PorTracker.PorTrackerBE.global.constant.SqliteSchema;
import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
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
public class AssetService {
        private final SqliteDatabaseManager sqliteManager;
        private final CurrencyService currencyService;
        private final AssetTypeService assetTypeService;

        // 기존 메서드 유지
        public List<AssetRecord> getAllAssets(String userId) {
                JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

                String sql = String.format("SELECT %s, %s, %s, %s, %s, %s, %s, %s, %s FROM %s",
                                SqliteSchema.COL_ID, SqliteSchema.COL_PUBLIC_ID,
                                SqliteSchema.COL_CREATED_AT, SqliteSchema.COL_UPDATED_AT,
                                SqliteSchema.COL_DELETED_AT, SqliteSchema.COL_NAME,
                                SqliteSchema.COL_DESCRIPTION, SqliteSchema.COL_CURRENCY_ID,
                                SqliteSchema.COL_TYPE_ID, SqliteSchema.TABLE_ASSET);

                return jdbcTemplate.query(sql, (rs, rowNum) -> AssetRecord.builder()
                                .id(rs.getLong(SqliteSchema.COL_ID))
                                .publicId(rs.getString(SqliteSchema.COL_PUBLIC_ID))
                                .createdAt(rs.getString(SqliteSchema.COL_CREATED_AT))
                                .updatedAt(rs.getString(SqliteSchema.COL_UPDATED_AT))
                                .deletedAt(rs.getString(SqliteSchema.COL_DELETED_AT))
                                .name(rs.getString(SqliteSchema.COL_NAME))
                                .description(rs.getString(SqliteSchema.COL_DESCRIPTION))
                                .currencyId(rs.getLong(SqliteSchema.COL_CURRENCY_ID))
                                .typeId(rs.getLong(SqliteSchema.COL_TYPE_ID)).build());
        }

        // NPE 버그 수정된 메서드
        @Transactional
        public void addAsset(String userId, AssetCreateRequest request) {
                JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

                // 로그 출력 수정
                log.info("Adding asset - currencyPublicId: {}, userId: {},typeid:{}",
                                request.getCurrencyId(), userId, request.getTypeId());

                CurrencyTypeRecord currency =
                                currencyService.getCurrencyById(userId, request.getCurrencyId());
                AssetTypeRecord assetType = assetTypeService.getAssetTypeIdByPublicId(userId,
                                request.getTypeId());

                if (currency == null || assetType == null) {
                        log.error("Failed to resolve IDs: currency={}, assetType={}", currency,
                                        assetType);
                        throw new BusinessException(ErrorCode.DATA_SAVE_FAILED);
                }

                String sql = String.format(
                                "INSERT OR IGNORE INTO %s (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)",
                                SqliteSchema.TABLE_ASSET, SqliteSchema.COL_PUBLIC_ID,
                                SqliteSchema.COL_NAME, SqliteSchema.COL_DESCRIPTION,
                                SqliteSchema.COL_CURRENCY_ID, SqliteSchema.COL_TYPE_ID);

                String publicId = UUID.randomUUID().toString();

                // update 시에도 람다 세터를 사용하여 NPE 방지
                jdbcTemplate.update(sql, ps -> {
                        ps.setString(1, publicId);
                        ps.setString(2, request.getName());
                        ps.setString(3, request.getDescription());
                        ps.setLong(4, currency.id());
                        ps.setLong(5, assetType.getId());
                });

                log.info("asset recorded successfully for user: {}, publicId: {}", userId,
                                publicId);
        }
}
