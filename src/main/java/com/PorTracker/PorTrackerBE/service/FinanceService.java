package com.PorTracker.PorTrackerBE.service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.PorTracker.PorTrackerBE.dto.AnonymizedStatsDto;
import com.PorTracker.PorTrackerBE.dto.TransactionDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinanceService {
    private final GoogleSheetService googleSheetService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StatisticsService statisticsService;
    private final ObjectMapper objectMapper;


    // 데이터 가져오고, 익명 통계 처리하기
    public List<TransactionDto> getAndContributeStats(String accessToken, String userId,
            String spreadsheetId) {
        String cacheKey = "user:data:" + userId;

        // Redis 캐시 확인
        Object rawData = redisTemplate.opsForValue().get(cacheKey);
        List<TransactionDto> transactions;

        if (rawData != null) {
            log.info("Redis 캐시에서 금융 데이터 로드");
            transactions = objectMapper.convertValue(rawData,
                    new TypeReference<List<TransactionDto>>() {});
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
            List<AnonymizedStatsDto> stats = statisticsService.anonymized(userId, transactions);
            log.info("익명 통계 생성 완료, 제공 데이터 수:{}", stats.size());

            // supabase에 저장
            statisticsService.contributeStats(stats);

        } catch (Exception e) {
            // 통계 처리 실패해도 데이터 넘기는 메인 로직은 중단 안되도록
            log.warn("통계 처리 중 오류", e);
        }
        return transactions;
    }
}
