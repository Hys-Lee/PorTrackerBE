package com.PorTracker.PorTrackerBE.service;

import com.PorTracker.PorTrackerBE.constant.ProfileSchema;
import com.PorTracker.PorTrackerBE.dto.AnonymizedStatsDto;
import com.PorTracker.PorTrackerBE.dto.ComparisonDto;
import com.PorTracker.PorTrackerBE.dto.GroupAverageResponse;
import com.PorTracker.PorTrackerBE.dto.TransactionDto;
import com.PorTracker.PorTrackerBE.repository.SupabaseRepository;
import com.PorTracker.PorTrackerBE.util.DateUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinanceService {
    private final GoogleSheetService googleSheetService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StatisticsService statisticsService;
    private final ObjectMapper objectMapper;
    private final SupabaseRepository supabaseRepository;

    // 데이터 가져오고, 익명 통계 처리하기
    public List<TransactionDto> getAndContributeStats(
            String accessToken, String userId, String spreadsheetId) {

        String cacheKey = "user:data:" + userId;

        // Redis 캐시 확인
        Object rawData = redisTemplate.opsForValue().get(cacheKey);
        List<TransactionDto> transactions;

        if (rawData != null) {
            log.info("Redis 캐시에서 금융 데이터 로드");
            transactions =
                    objectMapper.convertValue(
                            rawData, new TypeReference<List<TransactionDto>>() {});
        } else {
            log.info("구글 시트엥서 원본 로드");
            try {
                // 임시 범위
                transactions =
                        googleSheetService.getTransactions(accessToken, spreadsheetId, "A1:E100");

                // redis 에 10분 저장
                redisTemplate.opsForValue().set(cacheKey, transactions, 10, TimeUnit.MINUTES);

            } catch (Exception e) {
                log.error("데이터 로드 실패", e);
                return List.of();
            }
        }
        if (transactions.isEmpty()) {
            return transactions;
        }
        // 익명 통계 처리
        try {
            Map<String, Object> profile = supabaseRepository.getUserProfile(userId);

            List<AnonymizedStatsDto> stats =
                    statisticsService.anonymized(userId, transactions, profile);
            log.info("익명 통계 생성 완료, 제공 데이터 수:{}", stats.size());

            // supabase에 저장
            statisticsService.contributeStats(stats);

        } catch (Exception e) {
            // 통계 처리 실패해도 데이터 넘기는 메인 로직은 중단 안되도록
            log.warn("통계 처리 중 오류", e);
        }
        return transactions;
    }

    public List<ComparisonDto> getComparison(
            String accessToken, String userId, String spreadsheetId) {
        List<TransactionDto> myData = getAndContributeStats(accessToken, userId, spreadsheetId);

        Map<String, Long> myStats =
                myData.stream()
                        .collect(
                                Collectors.groupingBy(
                                        TransactionDto::category,
                                        Collectors.summingLong(TransactionDto::amount)));

        Map<String, Object> profile = supabaseRepository.getUserProfile(userId);
        String ageGroup =
                (String) profile.getOrDefault(ProfileSchema.AGE_GROUP, ProfileSchema.UNKNOWN);
        String jobType =
                (String) profile.getOrDefault(ProfileSchema.JOB_TYPE, ProfileSchema.UNKNOWN);
        String period = DateUtil.getCurrentPeriod();

        List<GroupAverageResponse> groupAvgs =
                supabaseRepository.getGroupAverages(ageGroup, jobType, period);

        return groupAvgs.stream()
                .map(
                        avg -> {
                            long myAcount = myStats.getOrDefault(avg.category(), 0L);
                            return new ComparisonDto(
                                    avg.category(),
                                    myAcount,
                                    avg.avgAmount(),
                                    myAcount - avg.avgAmount());
                        })
                .collect(Collectors.toList());
    }
}
