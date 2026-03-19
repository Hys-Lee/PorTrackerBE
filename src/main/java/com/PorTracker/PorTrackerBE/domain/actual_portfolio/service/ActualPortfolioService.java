package com.PorTracker.PorTrackerBE.domain.actual_portfolio.service;

import com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto.ActualPortfolioCreateRequest;
import com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto.ActualPortfolioSearchRequest;
import com.PorTracker.PorTrackerBE.domain.actual_portfolio.entity.ActualPortfolioRecord;
import com.PorTracker.PorTrackerBE.domain.actual_portfolio.repository.ActualPortfolioRepository;
import com.PorTracker.PorTrackerBE.domain.asset.entity.AssetRecord;
import com.PorTracker.PorTrackerBE.domain.asset.service.AssetService;
import com.PorTracker.PorTrackerBE.domain.currency.entity.CurrencyTypeRecord;
import com.PorTracker.PorTrackerBE.domain.currency.service.CurrencyService;
import com.PorTracker.PorTrackerBE.global.common.UserContextHolder;
import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import com.PorTracker.PorTrackerBE.global.infra.sqlite.SqliteDatabaseManager;
import com.PorTracker.PorTrackerBE.global.service.SyncService;
import com.PorTracker.PorTrackerBE.domain.memo.repository.MemoRepository;
import com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto.ActualPortfolioWithMemoCreateRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActualPortfolioService {
    private final SqliteDatabaseManager sqliteManager;
    private final ActualPortfolioRepository actualPortfolioRepository;
    private final AssetService assetService;
    private final CurrencyService currencyService;
    private final MemoRepository memoRepository;

    // test
    private final SyncService syncService;

    // public List<ActualPortfolioRecord> getAllActualPortfolios(String userId) {
    public List<ActualPortfolioRecord> getAllActualPortfolios() {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        return actualPortfolioRepository.findAll(jdbcTemplate);
    }

    public List<ActualPortfolioRecord> getUnlinkedActualPortfolios() {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        return actualPortfolioRepository.findUnlinkedToMemo(jdbcTemplate);
    }

    // public ActualPortfolioRecord getActualPortfolioById(String userId, String publicId) {
    public ActualPortfolioRecord getActualPortfolioById(String publicId) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        // try {
        return actualPortfolioRepository
                .findByPublicId(jdbcTemplate, publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA));
        // } catch (Exception e) {
        // log.error("ActualPortfolio 조회 중 에러 발생: {}", e.getMessage());
        // return null;
        // }
    }

    public List<ActualPortfolioRecord> getActualPortfolioByPublicIds(List<String> publicIds) {
        String userId = UserContextHolder.getUserId();
        org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate jdbcTemplate =
                sqliteManager.getNamedParameterJdbcTemplate(userId);

        return actualPortfolioRepository.findByPublicIds(jdbcTemplate, publicIds);
    }

    public List<ActualPortfolioRecord> search(ActualPortfolioSearchRequest request) {
        String userId = UserContextHolder.getUserId();

        NamedParameterJdbcTemplate jdbcTemplate =
                sqliteManager.getNamedParameterJdbcTemplate(userId);
        return actualPortfolioRepository.search(jdbcTemplate, request);
    }

    @Transactional
    // public void addActualPortfolio(String userId, ActualPortfolioCreateRequest request) {
    public String addActualPortfolio(ActualPortfolioCreateRequest request) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        // 여기 고쳐야 함. publicId를 실제 id값으로 찾아 넣을 수 있또록

        // assetService.
        AssetRecord assetRes =
                // assetService.getAssetByPublicId(userId, request.getAssetId());
                assetService.getAssetByPublicId(request.getAssetId());

        // CurrencyTypeRecord currencyRes = currencyService.getCurrencyByPublicId(userId,
        CurrencyTypeRecord currencyRes =
                currencyService.getCurrencyByPublicId(request.getCurrencyId());

        String publicId =
                actualPortfolioRepository.save(
                        jdbcTemplate, request, assetRes.getId(), currencyRes.id());

        log.info(
                "actual portfolio recorded successfully for user: {}, publicId: {}",
                userId,
                publicId);
        return publicId;
    }

    @Transactional
    // public void updateActualPortfolio(String userId, String publicId,
    public void updateActualPortfolio(String publicId, ActualPortfolioCreateRequest request) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        // Check exists
        actualPortfolioRepository
                .findByPublicId(jdbcTemplate, publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA));

        AssetRecord assetRes =
                // assetService.getAssetByPublicId(userId, request.getAssetId());
                assetService.getAssetByPublicId(request.getAssetId());
        // if (assetRes == null) {
        // throw new BusinessException(ErrorCode.NO_DATA, "Asset not found");
        // }

        // CurrencyTypeRecord currencyRes = currencyService.getCurrencyByPublicId(userId,
        CurrencyTypeRecord currencyRes =
                currencyService.getCurrencyByPublicId(request.getCurrencyId());
        // if (currencyRes == null) {
        // throw new BusinessException(ErrorCode.NO_DATA, "Currency not found");
        // }

        actualPortfolioRepository.updateByPublicId(
                jdbcTemplate, publicId, request, assetRes.getId(), currencyRes.id());

        log.info(
                "actual portfolio updated successfully for user: {}, publicId: {}",
                userId,
                publicId);
    }

    @Transactional
    public String addActualPortfolioWithMemo(ActualPortfolioWithMemoCreateRequest request) {
        String publicId = addActualPortfolio(request);
        if (request.getMemoId() != null) {
            String userId = UserContextHolder.getUserId();
            JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);
            ActualPortfolioRecord record = getActualPortfolioById(publicId);
            memoRepository.patchIdsByPublicId(jdbcTemplate, request.getMemoId(), record.getId(), null);
        }
        return publicId;
    }

    @Transactional
    public void updateActualPortfolioWithMemo(String publicId, ActualPortfolioWithMemoCreateRequest request) {
        updateActualPortfolio(publicId, request);

        // test
        String memoId = request.getMemoId();
        log.info("{} in updateActualPortfolioWithMemo: ",memoId);

        if (request.getMemoId() != null) {
            String userId = UserContextHolder.getUserId();
            JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);
            ActualPortfolioRecord record = getActualPortfolioById(publicId);
            memoRepository.patchIdsByPublicId(jdbcTemplate, request.getMemoId(), record.getId(), null);
        }
    }

    @Transactional
    // public void deleteActualPortfolio(String userId, String publicId) {
    public void deleteActualPortfolio(String publicId) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        // Check exists
        ActualPortfolioRecord record = actualPortfolioRepository
                .findByPublicId(jdbcTemplate, publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA));

        actualPortfolioRepository.deleteByPublicId(jdbcTemplate, publicId);

        memoRepository.nullifyActualId(jdbcTemplate, record.getId());

        log.info(
                "actual portfolio deleted successfully for user: {}, publicId: {}",
                userId,
                publicId);
    }
}
