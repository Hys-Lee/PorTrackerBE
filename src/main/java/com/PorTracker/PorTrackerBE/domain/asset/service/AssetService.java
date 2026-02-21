package com.PorTracker.PorTrackerBE.domain.asset.service;

import com.PorTracker.PorTrackerBE.domain.asset.dto.AssetCreateRequest;
import com.PorTracker.PorTrackerBE.domain.asset.entity.AssetRecord;
import com.PorTracker.PorTrackerBE.domain.asset.entity.AssetTypeRecord;
import com.PorTracker.PorTrackerBE.domain.asset.repository.AssetRepository;
import com.PorTracker.PorTrackerBE.domain.currency.entity.CurrencyTypeRecord;
import com.PorTracker.PorTrackerBE.domain.currency.service.CurrencyService;
import com.PorTracker.PorTrackerBE.global.common.UserContextHolder;
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

    private final AssetRepository assetRepository;

    // 기존 메서드 유지
    // public List<AssetRecord> getAllAssets(String userId) {
    public List<AssetRecord> getAllAssets() {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        return assetRepository.findAll(jdbcTemplate);
    }

    // public AssetRecord getAssetByPublicId(String userId, String publicId) {
    public AssetRecord getAssetByPublicId(String publicId) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        return assetRepository
                .findByPublicId(jdbcTemplate, publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA, "assets"));
    }

    // NPE 버그 수정된 메서드
    @Transactional
    // public void addAsset(String userId, AssetCreateRequest request) {
    public void addAsset(AssetCreateRequest request) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        // 로그 출력 수정
        log.info(
                "Adding asset - currencyPublicId: {}, userId: {},typeid:{}",
                request.getCurrencyId(),
                userId,
                request.getTypeId());

        // CurrencyTypeRecord currency = currencyService.getCurrencyByPublicId(userId,
        CurrencyTypeRecord currency =
                currencyService.getCurrencyByPublicId(request.getCurrencyId());
        // AssetTypeRecord assetType = assetTypeService.getAssetTypeIdByPublicId(userId,
        AssetTypeRecord assetType = assetTypeService.getAssetTypeIdByPublicId(request.getTypeId());

        if (currency == null || assetType == null) {
            log.error("Failed to resolve IDs: currency={}, assetType={}", currency, assetType);
            throw new BusinessException(ErrorCode.DATA_SAVE_FAILED);
        }

        String sql =
                String.format(
                        "INSERT OR IGNORE INTO %s (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)",
                        SqliteSchema.TABLE_ASSET,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_NAME,
                        SqliteSchema.COL_DESCRIPTION,
                        SqliteSchema.COL_CURRENCY_ID,
                        SqliteSchema.COL_TYPE_ID);

        String publicId = UUID.randomUUID().toString();

        // update 시에도 람다 세터를 사용하여 NPE 방지
        jdbcTemplate.update(
                sql,
                ps -> {
                    ps.setString(1, publicId);
                    ps.setString(2, request.getName());
                    ps.setString(3, request.getDescription());
                    ps.setLong(4, currency.id());
                    ps.setLong(5, assetType.getId());
                });

        log.info("asset recorded successfully for user: {}, publicId: {}", userId, publicId);
    }

    @Transactional
    // public void updateAsset(String userId, String publicId, AssetCreateRequest request) {
    public void updateAsset(String publicId, AssetCreateRequest request) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        // Check exists
        assetRepository
                .findByPublicId(jdbcTemplate, publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA, "assets"));

        // CurrencyTypeRecord currency = currencyService.getCurrencyByPublicId(userId,
        CurrencyTypeRecord currency =
                currencyService.getCurrencyByPublicId(request.getCurrencyId());
        // AssetTypeRecord assetType = assetTypeService.getAssetTypeIdByPublicId(userId,
        AssetTypeRecord assetType = assetTypeService.getAssetTypeIdByPublicId(request.getTypeId());

        if (currency == null || assetType == null) {
            throw new BusinessException(ErrorCode.DATA_SAVE_FAILED);
        }

        assetRepository.updateByPublicId(
                jdbcTemplate,
                publicId,
                request.getName(),
                request.getDescription(),
                currency.id(),
                assetType.getId());

        log.info("asset updated successfully for user: {}, publicId: {}", userId, publicId);
    }

    @Transactional
    // public void deleteAsset(String userId, String publicId) {
    public void deleteAsset(String publicId) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        // Check exists
        assetRepository
                .findByPublicId(jdbcTemplate, publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA, "assets"));

        assetRepository.deleteByPublicId(jdbcTemplate, publicId);

        log.info("asset deleted successfully for user: {}, publicId: {}", userId, publicId);
    }
}
