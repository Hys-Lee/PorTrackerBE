package com.PorTracker.PorTrackerBE.service;

import com.PorTracker.PorTrackerBE.constant.ProfileSchema;
import com.PorTracker.PorTrackerBE.dto.AnonymizedStatsDto;
import com.PorTracker.PorTrackerBE.dto.TransactionDto;
import com.PorTracker.PorTrackerBE.repository.SupabaseRepository;
import com.PorTracker.PorTrackerBE.util.DateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        public void contributeStats(List<AnonymizedStatsDto> statsList) {
                if (statsList == null || statsList.isEmpty())
                        return;

                // WebClient webClient =
                // WebClient.builder().baseUrl(supabaseUrl).defaultHeader("apikey", supabaseKey)
                // .defaultHeader("Authorization", "Bearer " + supabaseKey)
                // .defaultHeader("Content-Type", "application/json")
                // // 중복 데이터 발생 시 업데이트 수행이라는데.
                // .defaultHeader(("Prefer"), "resolution=merge-duplicates").build();

                // try {
                // webClient.post().uri("/rest/v1/category_stats").bodyValue(statsList).retrieve()
                // .toBodilessEntity().block(); // 성고할 때까지 대기

                // log.info("supabase category_Stats ssaving complete! listSize:{}",
                // statsList.size());
                // } catch (Exception e) {
                // log.error("supabase statics saving error:{} ", e.getMessage());
                // }


                // [추가] 실제로 날아가는 JSON 모양을 로그로 찍어봅니다.
                // try {
                // ObjectMapper mapper = new ObjectMapper();
                // // 전역 설정과 동일하게 Snake Case로 변환해서 출력해봅니다.
                // mapper.setPropertyNamingStrategy(
                // com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE);
                // String json = mapper.writeValueAsString(statsList);
                // log.info("Supabase로 전송할 데이터: {}", json);
                // } catch (Exception e) {
                // log.error("JSON 변환 실패");
                // }


                // Repository 통해 저장
                supabaseRepository.upsertCategoryStats(statsList);
                log.info("supabase contribute success: {}", statsList.size());
        }

        private Map<String, Long> aggregate(List<TransactionDto> transactions) {
                return transactions.stream().collect(Collectors.groupingBy(TransactionDto::category,
                                Collectors.summingLong(TransactionDto::amount)));
        }

        // 비식별 통계 객체 리스트로 변환하기
        public List<AnonymizedStatsDto> anonymized(String userId, List<TransactionDto> transactions,
                        Map<String, Object> profile) {
                String hashedId = hashUserId(userId);



                // 프로필에서 그룹 정보 추출
                String ageGroup = (String) profile.getOrDefault(ProfileSchema.AGE_GROUP,
                                ProfileSchema.UNKNOWN);
                String jobType = (String) profile.getOrDefault(ProfileSchema.JOB_TYPE,
                                ProfileSchema.UNKNOWN);

                String period = DateUtil.getCurrentPeriod();

                // 카테고리별 집계
                Map<String, Long> aggregated = transactions.stream()
                                .collect(Collectors.groupingBy(TransactionDto::category,
                                                Collectors.summingLong((TransactionDto::amount))));

                return aggregated.entrySet().stream()
                                .map(entry -> new AnonymizedStatsDto(hashedId, period,
                                                entry.getKey(), entry.getValue(), ageGroup,
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
