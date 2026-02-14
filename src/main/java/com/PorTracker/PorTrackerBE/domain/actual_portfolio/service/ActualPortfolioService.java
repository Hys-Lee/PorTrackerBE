package com.PorTracker.PorTrackerBE.domain.actual_portfolio.service;

import com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto.ActualPortfolioCreateRequest;
import com.PorTracker.PorTrackerBE.domain.actual_portfolio.entity.ActualPortfolioRecord;
import com.PorTracker.PorTrackerBE.domain.actual_portfolio.repository.ActualPortfolioRepository;
import com.PorTracker.PorTrackerBE.domain.asset.entity.AssetRecord;
import com.PorTracker.PorTrackerBE.domain.asset.service.AssetService;
import com.PorTracker.PorTrackerBE.domain.currency.entity.CurrencyTypeRecord;
import com.PorTracker.PorTrackerBE.domain.currency.service.CurrencyService;
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
public class ActualPortfolioService {
    private final SqliteDatabaseManager sqliteManager;
    private final ActualPortfolioRepository actualPortfolioRepository;
    private final AssetService assetService;
    private final CurrencyService currencyService;

    public List<ActualPortfolioRecord> getAllActualPortfolios(String userId) {
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        return actualPortfolioRepository.findAll(jdbcTemplate);
    }

    public ActualPortfolioRecord getActualPortfolioById(String userId, String publicId) {
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        // try {
        return actualPortfolioRepository
                .findByPublicId(jdbcTemplate, publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA));
        // } catch (Exception e) {
        // log.error("ActualPortfolio 조회 중 에러 발생: {}", e.getMessage());
        // return null;
        // }
    }

    @Transactional
    public void addActualPortfolio(String userId, ActualPortfolioCreateRequest request) {
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        // 여기 고쳐야 함. publicId를 실제 id값으로 찾아 넣을 수 있또록

        // assetService.
        AssetRecord assetRes = assetService.getAssetByPublicId(userId, request.getAssetId());
        CurrencyTypeRecord currencyRes =
                currencyService.getCurrencyByPublicId(userId, request.getCurrencyId());

        actualPortfolioRepository.save(jdbcTemplate, request, assetRes.getId(), currencyRes.id());

        log.info("actual portfolio recorded successfully for user: {}", userId);
    }
}
