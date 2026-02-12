package com.PorTracker.PorTrackerBE.service;

import com.PorTracker.PorTrackerBE.constant.ProfileSchema;
import com.PorTracker.PorTrackerBE.dto.AnonymizedStatsDto;
import com.PorTracker.PorTrackerBE.dto.ComparisonDto;
import com.PorTracker.PorTrackerBE.dto.GroupAverageResponse;
import com.PorTracker.PorTrackerBE.dto.TransactionDto;
import com.PorTracker.PorTrackerBE.global.aspect.DistributedLock;
import com.PorTracker.PorTrackerBE.global.constant.FileConstants;
import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import com.PorTracker.PorTrackerBE.repository.SqliteRepository;
import com.PorTracker.PorTrackerBE.repository.SupabaseRepository;
import com.PorTracker.PorTrackerBE.service.sqlite.SqliteDatabaseManager;
import com.PorTracker.PorTrackerBE.util.DateUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinanceService {
    private final GoogleSheetService googleSheetService;
    private final GoogleDriveService googleDriveService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StatisticsService statisticsService;
    private final ObjectMapper objectMapper;
    private final SupabaseRepository supabaseRepository;
    private final SqliteDatabaseManager sqliteManager;

    // db 생성하기
    public String initializeUserStorage(String accessToken, String userId) {
        String fileName = userId + FileConstants.DB_FILE_SUFFIX;

        String fileId = googleDriveService.findFileIdByName(fileName, accessToken);

        if (fileId != null)
            return fileId;

        // 없으면 데이터 담긴 파일 생성
        String tempPath = FileConstants.DB_STORAGE_ROOT + userId + "_init.db"; // 임시 파일 위치
        sqliteManager.createInitialFile(tempPath); // 파일 생성 및 스키마 주입

        // google drive에 옮기기
        String newFileId = googleDriveService.uploadFile(fileName, tempPath, accessToken);

        new java.io.File((tempPath)).delete(); // 임시 파일 제거
        return newFileId;
    }

    // 데이터 가져오고, 익명 통계 처리하기
    @DistributedLock(key = "#userId")
    public List<TransactionDto> getAndContributeStats(String accessToken, String userId,
            String fileId, boolean refresh) {

        // 데이터 가져오기
        String cacheKey = "finance:data:" + userId + ":" + fileId; // 유저, 시트
        // 별 고유하도록

        // refresh 수동 갱신에 대해 기존 캐시 삭제처리
        if (refresh) {
            log.info("cache forcefully remove req - key:{}", cacheKey);
            redisTemplate.delete(cacheKey);
        }

        // Redis 캐시 확인
        Object rawData = redisTemplate.opsForValue().get(cacheKey);
        List<TransactionDto> transactions;

        if (rawData != null) {
            log.info("Redis cache hit! - finance data");
            transactions = objectMapper.convertValue(rawData,
                    new TypeReference<List<TransactionDto>>() {});
        } else {
            log.info("redis cache miss! - start downloading SQLite file");

            transactions = getUserTransactions(userId, fileId, accessToken);

            if (!transactions.isEmpty()) {
                // redis 에 1시간 저장
                redisTemplate.opsForValue().set(cacheKey, transactions, 1, TimeUnit.HOURS);
            }
        }

        // 익명 통계 기여
        contributeStatsIfPresent(userId, transactions);

        return transactions;

        // } finally {
        // redisTemplate.delete(lockKey);
        // }
    }

    public List<ComparisonDto> getComparison(String accessToken, String userId,
            String spreadsheetId, boolean refresh) {
        List<TransactionDto> myData =
                getAndContributeStats(accessToken, userId, spreadsheetId, refresh);

        Map<String, Long> myStats =
                myData.stream().collect(Collectors.groupingBy(TransactionDto::category,
                        Collectors.summingLong(TransactionDto::amount)));

        Map<String, Object> profile = supabaseRepository.getUserProfile(userId);
        String ageGroup =
                (String) profile.getOrDefault(ProfileSchema.AGE_GROUP, ProfileSchema.UNKNOWN);
        String jobType =
                (String) profile.getOrDefault(ProfileSchema.JOB_TYPE, ProfileSchema.UNKNOWN);
        String period = DateUtil.getCurrentPeriod();

        List<GroupAverageResponse> groupAvgs =
                supabaseRepository.getGroupAverages(ageGroup, jobType, period);

        return groupAvgs.stream().map(avg -> {
            long myAcount = myStats.getOrDefault(avg.category(), 0L);
            return new ComparisonDto(avg.category(), myAcount, avg.avgAmount(),
                    myAcount - avg.avgAmount());
        }).collect(Collectors.toList());
    }

    /// 다른 버전
    private final SqliteDatabaseManager dbManager;
    private final SqliteRepository sqliteRepository;

    public List<TransactionDto> getUserTransactions(String userId, String fileId, String token) {
        // 로컬 파일 처리
        String dbPath = Paths.get(FileConstants.DB_STORAGE_ROOT, userId + ".db").toString();
        File dbFile = new File(dbPath);

        // 파일 동기화
        try {

            if (!dbFile.exists()
                    || googleDriveService.isNewerVersionAvailable(fileId, dbPath, token)) {
                log.info("user[{}]'s db file downloaded from google drive", userId);
                googleDriveService.downloadDatabaseFile(fileId, dbPath, token);
            }
        } catch (Exception e) {

            log.error("error occured white db sync: {}", e.getMessage());
            throw new BusinessException(ErrorCode.FILE_DOWNLOAD_FAILED);
        }
        try {
            // 1. Supabase에서 유저 프로필(base_currency 포함) 조회
            Map<String, Object> profile = supabaseRepository.getUserProfile(userId);



            String userBaseCurrency = (String) profile.get("base_currency"); // 예: 'KRW'
            // 유저 db연결
            DataSource userDataSource =
                    dbManager.getJdbcTemplateOfDataSource(userId, userBaseCurrency).getDataSource();

            // 데이터 조회
            return sqliteRepository.findAllTransactions(userDataSource);
        } catch (Exception e) {
            log.error("SQLite data load failed", e);
            throw new BusinessException((ErrorCode.DB_SYNC_FAILED));
        }
    }

    // 통계 처리 부분
    private void contributeStatsIfPresent(String userId, List<TransactionDto> transactions) {
        if (transactions.isEmpty()) {
            return;
        }
        // 익명 통계 처리
        try {
            Map<String, Object> profile = supabaseRepository.getUserProfile(userId);

            List<AnonymizedStatsDto> stats =
                    statisticsService.anonymized(userId, transactions, profile);
            log.info("created anonymous stats, data counts:{}", stats.size());

            // supabase에 저장 -> 비동기 처리
            statisticsService.contributeStats(stats);

        } catch (Exception e) {
            // 통계 처리 실패해도 데이터 넘기는 메인 로직은 중단 안되도록
            log.warn("Error At Stats Processing", e);
        }
    }

    public void saveTransaction(String userId, String fileId, String token, TransactionDto dto) {
        String dbPath = Paths.get(FileConstants.DB_STORAGE_ROOT, userId + ".db").toString();

        // 임시 currency
        DataSource ds = sqliteManager.getJdbcTemplateOfDataSource(userId, "KRW").getDataSource();

        sqliteRepository.insertTmpTransaction(ds, dto);
        log.info("Success to save sqlite data: {}", dto);
    }
}
