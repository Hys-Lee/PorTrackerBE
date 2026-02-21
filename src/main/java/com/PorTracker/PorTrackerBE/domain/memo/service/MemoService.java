package com.PorTracker.PorTrackerBE.domain.memo.service;

import com.PorTracker.PorTrackerBE.domain.actual_portfolio.service.ActualPortfolioService;
import com.PorTracker.PorTrackerBE.domain.memo.dto.MemoCreateRequest;
import com.PorTracker.PorTrackerBE.domain.memo.entity.MemoRecord;
import com.PorTracker.PorTrackerBE.domain.memo.repository.MemoRepository;
import com.PorTracker.PorTrackerBE.domain.target_portfolio.service.TargetPortfolioService;
import com.PorTracker.PorTrackerBE.global.common.UserContextHolder;
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
public class MemoService {
    private final SqliteDatabaseManager sqliteManager;
    private final MemoRepository memoRepository;
    // private final ActualPortfolioRepository actualPortfolioRepository;
    // private final TargetPortfolioRepository targetPortfolioRepository;
    private final ActualPortfolioService actualPortfolioService;
    private final TargetPortfolioService targetPortfolioService;

    // public List<MemoRecord> getAllMemos(String userId) {
    public List<MemoRecord> getAllMemos() {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);
        return memoRepository.findAll(jdbcTemplate);
    }

    // public MemoRecord getMemoById(String userId, String publicId) {
    public MemoRecord getMemoById(String publicId) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);
        return memoRepository
                .findByPublicId(jdbcTemplate, publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA));
    }

    @Transactional
    // public String addMemo(String userId, MemoCreateRequest request) {
    public String addMemo(MemoCreateRequest request) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        // Long actualId = resolveActualPortfolioId(jdbcTemplate, request.getActualId());
        // Long targetId = resolveTargetPortfolioId(jdbcTemplate, request.getTargetId());
        String publicId = UUID.randomUUID().toString();

        Long actualId = null;
        // tset
        log.info("actualId in addmemo before: {}", request.getActualId());
        if (request.getActualId() != null) {

            // actualId = actualPortfolioService.getActualPortfolioById(userId,
            // request.getActualId())
            actualId = actualPortfolioService.getActualPortfolioById(request.getActualId()).getId();
        }
        // tset
        log.info("actualId in addmemo after: {}", actualId);

        Long targetId = null;
        if (request.getTargetId() != null) {

            targetId =
                    targetPortfolioService
                            // .getTargetPortfolioDetail(userId,
                            // request.getTargetId()).portfolio().getId();
                            .getTargetPortfolioDetail(request.getTargetId())
                            .portfolio()
                            .getId();
        }

        memoRepository.save(jdbcTemplate, request, publicId, actualId, targetId);

        log.info("Memo created successfully for user: {}, publicId: {}", userId, publicId);
        return publicId;
    }

    @Transactional
    // public void updateMemo(String userId, String publicId, MemoCreateRequest request) {
    public void updateMemo(String publicId, MemoCreateRequest request) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        // 메모 존재 여부 확인
        memoRepository
                .findByPublicId(jdbcTemplate, publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA));

        Long actualId = null;
        if (request.getActualId() != null) {
            // actualId = actualPortfolioService.getActualPortfolioById(userId,
            // request.getActualId())
            actualId = actualPortfolioService.getActualPortfolioById(request.getActualId()).getId();
        }

        Long targetId = null;
        if (request.getTargetId() != null) {
            // Null check for the result of getTargetPortfolioDetail might be needed if it can
            // return null
            var detail =
                    // targetPortfolioService.getTargetPortfolioDetail(userId,
                    // request.getTargetId());
                    targetPortfolioService.getTargetPortfolioDetail(request.getTargetId());
            if (detail != null && detail.portfolio() != null) {
                targetId = detail.portfolio().getId();
            }
        }

        memoRepository.updateByPublicId(jdbcTemplate, publicId, request, actualId, targetId);

        log.info("Memo updated successfully for user: {}, publicId: {}", userId, publicId);
    }

    @Transactional
    // public void deleteMemo(String userId, String publicId) {
    public void deleteMemo(String publicId) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplateOfDataSource(userId);

        // 메모 존재 여부 확인
        memoRepository
                .findByPublicId(jdbcTemplate, publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA));

        memoRepository.deleteByPublicId(jdbcTemplate, publicId);

        log.info("Memo deleted successfully for user: {}, publicId: {}", userId, publicId);
    }

    // private Long resolveActualPortfolioId(JdbcTemplate jdbcTemplate, String publicId) {
    // if (publicId == null || publicId.isEmpty()) {
    // return null;
    // }
    // return actualPortfolioRepository.findByPublicId(jdbcTemplate, publicId).map(
    // com.PorTracker.PorTrackerBE.domain.actual_portfolio.entity.ActualPortfolioRecord::getId)
    // .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA));
    // }

    // private Long resolveTargetPortfolioId(JdbcTemplate jdbcTemplate, String publicId) {
    // if (publicId == null || publicId.isEmpty()) {
    // return null;
    // }
    // return targetPortfolioRepository.findByPublicId(jdbcTemplate, publicId).map(
    // com.PorTracker.PorTrackerBE.domain.target_portfolio.entity.TargetPortfolioRecord::getId)
    // .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA));
    // }
}
