package com.PorTracker.PorTrackerBE.service;

import com.PorTracker.PorTrackerBE.constant.ProfileSchema;
import com.PorTracker.PorTrackerBE.dto.AnonymizedStatsDto;
import com.PorTracker.PorTrackerBE.dto.TransactionDto;
import com.PorTracker.PorTrackerBE.repository.SupabaseRepository;
import com.PorTracker.PorTrackerBE.util.DateUtil;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatisticsService {

    // @Value("${supabase.url}")
    // private String supabaseUrl;

    // @Value("${supabase.key}")
    // private String supabaseKey;

    private final SupabaseRepository supabaseRepository;

    @Async("statsExecutor")
    public void contributeStats(List<AnonymizedStatsDto> statsList) {
        if (statsList == null || statsList.isEmpty()) return;

        try {
            // Repository 통해 저장
            supabaseRepository.upsertCategoryStats(statsList);
            log.info("supabase contribute success: {}", statsList.size());
        } catch (Exception e) {
            log.error("Error while saving stats (async): {}", e.getMessage());
        }
    }

    private Map<String, Long> aggregate(List<TransactionDto> transactions) {
        return transactions.stream()
                .collect(
                        Collectors.groupingBy(
                                TransactionDto::category,
                                Collectors.summingLong(TransactionDto::amount)));
    }

    // 비식별 통계 객체 리스트로 변환하기
    public List<AnonymizedStatsDto> anonymized(
            String userId, List<TransactionDto> transactions, Map<String, Object> profile) {
        String hashedId = hashUserId(userId);

        // 프로필에서 그룹 정보 추출
        String ageGroup =
                (String) profile.getOrDefault(ProfileSchema.AGE_GROUP, ProfileSchema.UNKNOWN);
        String jobType =
                (String) profile.getOrDefault(ProfileSchema.JOB_TYPE, ProfileSchema.UNKNOWN);

        String period = DateUtil.getCurrentPeriod();

        // 카테고리별 집계
        Map<String, Long> aggregated =
                transactions.stream()
                        .collect(
                                Collectors.groupingBy(
                                        TransactionDto::category,
                                        Collectors.summingLong((TransactionDto::amount))));

        return aggregated.entrySet().stream()
                .map(
                        entry ->
                                new AnonymizedStatsDto(
                                        hashedId,
                                        period,
                                        entry.getKey(),
                                        entry.getValue(),
                                        ageGroup,
                                        jobType))
                .collect(Collectors.toList());
    }

    private String hashUserId(String userId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(userId.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return "anonymous";
        }
    }
}
