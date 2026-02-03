package com.PorTracker.PorTrackerBE.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.PorTracker.PorTrackerBE.dto.AnonymizedStatsDto;
import com.PorTracker.PorTrackerBE.dto.TransactionDto;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StatisticsService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    public void contributeStats(List<AnonymizedStatsDto> statsList) {
        if (statsList == null || statsList.isEmpty())
            return;

        WebClient webClient =
                WebClient.builder().baseUrl(supabaseUrl).defaultHeader("apikey", supabaseKey)
                        .defaultHeader("Authorization", "Bearer " + supabaseKey)
                        .defaultHeader("Content-Type", "application/json")
                        // 중복 데이터 발생 시 업데이트 수행이라는데.
                        .defaultHeader(("Prefer"), "resolution=merge-duplicates").build();


        try {
            webClient.post().uri("/rest/v1/category_stats").bodyValue(statsList).retrieve()
                    .toBodilessEntity().block(); // 성고할 때까지 대기

            log.info("supabase category_Stats ssaving complete! listSize:{}", statsList.size());
        } catch (Exception e) {
            log.error("supabase statics saving error:{} ", e.getMessage());
        }

    }


    // 비식별 통계 객체 리스트로 변환하기
    public List<AnonymizedStatsDto> anonymized(String userId, List<TransactionDto> transactions) {
        String hashedId = hashUserId(userId);

        // 카테고리별 집계
        Map<String, Long> aggregated =
                transactions.stream().collect(Collectors.groupingBy(TransactionDto::category,
                        Collectors.summingLong((TransactionDto::amount))));

        return aggregated.entrySet().stream().map(entry -> new AnonymizedStatsDto(hashedId,
                "2026-02", entry.getKey(), entry.getValue())).collect(Collectors.toList());

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
