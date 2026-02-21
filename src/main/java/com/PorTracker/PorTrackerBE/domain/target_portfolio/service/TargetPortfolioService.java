package com.PorTracker.PorTrackerBE.domain.target_portfolio.service;

import com.PorTracker.PorTrackerBE.domain.asset.entity.AssetRecord;
import com.PorTracker.PorTrackerBE.domain.asset.service.AssetService;
import com.PorTracker.PorTrackerBE.domain.target_portfolio.dto.TargetPortfolioCreateRequest;
import com.PorTracker.PorTrackerBE.domain.target_portfolio.dto.TargetPortfolioItemRequest;
import com.PorTracker.PorTrackerBE.domain.target_portfolio.dto.TargetPortfolioSnapshotUpdateRequest;
import com.PorTracker.PorTrackerBE.domain.target_portfolio.entity.TargetPortfolioItemRecord;
import com.PorTracker.PorTrackerBE.domain.target_portfolio.entity.TargetPortfolioRecord;
import com.PorTracker.PorTrackerBE.domain.target_portfolio.repository.TargetPortfolioItemRepository;
import com.PorTracker.PorTrackerBE.domain.target_portfolio.repository.TargetPortfolioRepository;
import com.PorTracker.PorTrackerBE.domain.target_portfolio.repository.TargetPortfolioSnapshotRepository;
import com.PorTracker.PorTrackerBE.global.common.UserContextHolder;
import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import com.PorTracker.PorTrackerBE.service.sqlite.SqliteDatabaseManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TargetPortfolioService {
    private final SqliteDatabaseManager sqliteManager;
    private final TargetPortfolioRepository targetPortfolioRepository;
    private final TargetPortfolioSnapshotRepository snapshotRepository;
    private final TargetPortfolioItemRepository itemRepository;
    private final AssetService assetService;

    /** 포트폴리오 목록과 각 포트폴리오의 최신 아이템들을 함께 조회 (N+1 문제 해결) */
    public List<com.PorTracker.PorTrackerBE.domain.target_portfolio.dto.TargetPortfolioData>
            getAllTargetPortfoliosFullData(
            // String userId) {
            ) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        // 1. 모든 포트폴리오 조회
        List<TargetPortfolioRecord> portfolios = targetPortfolioRepository.findAll(jdbcTemplate);

        if (portfolios.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 2. 포트폴리오 ID 목록 추출
        List<Long> portfolioIds = portfolios.stream().map(TargetPortfolioRecord::getId).toList();

        // 3. 모든 포트폴리오의 최신 아이템 한 번에 조회 (Map<PortfolioId, List<Item>>)
        java.util.Map<Long, List<TargetPortfolioItemRecord>> itemsMap =
                itemRepository.findLatestItemsByPortfolioIds(jdbcTemplate, portfolioIds);

        // 4. 데이터 조립
        return portfolios.stream()
                .map(
                        portfolio ->
                                new com.PorTracker.PorTrackerBE.domain
                                        .target_portfolio
                                        .dto
                                        .TargetPortfolioData(
                                        portfolio,
                                        itemsMap.getOrDefault(
                                                portfolio.getId(),
                                                java.util.Collections.emptyList())))
                .toList();
    }

    /** 단일 포트폴리오 상세 조회 (Full Data) */
    public com.PorTracker.PorTrackerBE.domain.target_portfolio.dto.TargetPortfolioData
            getTargetPortfolioDetail(
                    // String userId, String publicId) {
                    String publicId) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        TargetPortfolioRecord portfolio =
                targetPortfolioRepository
                        .findByPublicId(jdbcTemplate, publicId)
                        .orElseThrow(
                                () -> new BusinessException(ErrorCode.NO_DATA, "target_portfolio"));

        List<TargetPortfolioItemRecord> items =
                itemRepository.findItemsByLatestSnapshot(jdbcTemplate, portfolio.getId());

        return new com.PorTracker.PorTrackerBE.domain.target_portfolio.dto.TargetPortfolioData(
                portfolio, items);
    }

    /** 타겟 포트폴리오의 최신 스냅샷에 등록된 아이템 목록 조회. item 테이블에서 서브쿼리로 최신 snapshot_id를 찾아 JOIN하여 한번에 가져옴. */
    // public List<TargetPortfolioItemRecord> getLatestSnapshotItems(String userId,
    public List<TargetPortfolioItemRecord> getLatestSnapshotItems(String publicId) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        TargetPortfolioRecord portfolio =
                targetPortfolioRepository
                        .findByPublicId(jdbcTemplate, publicId)
                        .orElseThrow(
                                () -> new BusinessException(ErrorCode.NO_DATA, "target_portfolio"));

        return itemRepository.findItemsByLatestSnapshot(jdbcTemplate, portfolio.getId());
    }

    /**
     * 타겟 포트폴리오 생성 + 첫 번째 스냅샷 + 아이템들 한 번에 생성
     *
     * @return 생성된 포트폴리오의 publicId
     */
    @Transactional
    // public String addTargetPortfolio(String userId, TargetPortfolioCreateRequest request) {
    public String addTargetPortfolio(TargetPortfolioCreateRequest request) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        String publicId = java.util.UUID.randomUUID().toString();

        // 1. 포트폴리오 생성
        Long portfolioId =
                targetPortfolioRepository.save(
                        jdbcTemplate, request.getName(), request.getDate(), publicId);

        // 2. 첫 번째 스냅샷 생성
        Long snapshotId = snapshotRepository.save(jdbcTemplate, portfolioId);

        // 3. 아이템들 저장
        // saveItems(jdbcTemplate, userId, snapshotId, request.getItems());
        saveItems(jdbcTemplate, snapshotId, request.getItems());

        log.info(
                "target portfolio created successfully for user: {}, publicId: {}",
                userId,
                publicId);

        return publicId;
    }

    /** 기존 타겟 포트폴리오에 새 스냅샷 추가 (비중 업데이트) */
    @Transactional
    // public void addSnapshot(String userId, String publicId,
    public void addSnapshot(String publicId, TargetPortfolioSnapshotUpdateRequest request) {

        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        TargetPortfolioRecord portfolio =
                targetPortfolioRepository
                        .findByPublicId(jdbcTemplate, publicId)
                        .orElseThrow(
                                () -> new BusinessException(ErrorCode.NO_DATA, "target_portfolio"));

        // 1. 새 스냅샷 생성
        Long snapshotId = snapshotRepository.save(jdbcTemplate, portfolio.getId());

        // 2. 아이템들 저장
        // saveItems(jdbcTemplate, userId, snapshotId, request.getItems());
        saveItems(jdbcTemplate, snapshotId, request.getItems());

        log.info("target portfolio snapshot added for user: {}, portfolio: {}", userId, publicId);
    }

    @Transactional
    // public void updateTargetPortfolio(String userId, String publicId,
    public void updateTargetPortfolio(String publicId, TargetPortfolioCreateRequest request) {

        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        // Check exists
        targetPortfolioRepository
                .findByPublicId(jdbcTemplate, publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA, "target_portfolio"));

        // Update name and date in target_portfolio table
        targetPortfolioRepository.updateByPublicId(
                jdbcTemplate, publicId, request.getName(), request.getDate());

        // For items, logically a "PUT" on a portfolio might mean updating the current
        // items.
        // However, the existing logic uses snapshots. Updating the "current" items implies
        // creating a NEW snapshot
        // or updating the latest snapshot?
        // Given the instructions, we'll assume updating the portfolio METADATA (name/date).
        // If items need update, existing addSnapshot logic handles it or we might need
        // another strategy.
        // But usually PUT /portfolios/:id updates the portfolio entity itself.
        // If the user wants to update items, they might use the snapshot endpoint or we
        // trigger a snapshot?
        // Let's assume updating metadata for now as per repository method availability.

        log.info("target portfolio updated for user: {}, portfolio: {}", userId, publicId);
    }

    /** 타겟 포트폴리오 소프트 삭제 */
    @Transactional
    // public void deleteTargetPortfolio(String userId, String publicId) {
    public void deleteTargetPortfolio(String publicId) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        targetPortfolioRepository
                .findByPublicId(jdbcTemplate, publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA));

        targetPortfolioRepository.deleteByPublicId(jdbcTemplate, publicId);

        log.info("target portfolio deleted for user: {}, portfolio: {}", userId, publicId);
    }

    /** 아이템 리스트 저장 (포트폴리오 생성, 스냅샷 추가 시 공통으로 사용) */
    // private void saveItems(JdbcTemplate jdbcTemplate, String userId, Long snapshotId,
    private void saveItems(
            JdbcTemplate jdbcTemplate, Long snapshotId, List<TargetPortfolioItemRequest> items) {

        if (items == null) {
            return;
        }
        for (TargetPortfolioItemRequest item : items) {
            AssetRecord asset =
                    // assetService.getAssetByPublicId(userId, item.getAssetId());
                    assetService.getAssetByPublicId(item.getAssetId());
            itemRepository.save(
                    jdbcTemplate, snapshotId, asset.getId(), item.getCurrentRatioBp()
                    // item.getRatioDeltaBp()
                    );
        }
    }
}
