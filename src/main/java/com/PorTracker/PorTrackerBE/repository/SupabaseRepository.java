package com.PorTracker.PorTrackerBE.repository;

import com.PorTracker.PorTrackerBE.constant.ProfileSchema;
import com.PorTracker.PorTrackerBE.constant.StatsSchema;
import com.PorTracker.PorTrackerBE.dto.GroupAverageResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;

@Repository
@RequiredArgsConstructor
public class SupabaseRepository {
    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    private final WebClient.Builder webClientBuilder;

    // 공용 WebClient 빌더
    private WebClient getWebClient() {
        // return WebClient.builder()
        return webClientBuilder.baseUrl(supabaseUrl).defaultHeader("apikey", supabaseKey)
                .defaultHeader("Authorization", "Bearer " + supabaseKey)
                .defaultHeader("Content-Type", "application/json").build();
    }

    // 통계 데이터 upsert
    public void upsertCategoryStats(List<?> statsList) {
        getWebClient().post().uri("/rest/v1/" + StatsSchema.TABLE_NAME)
                .header("Prefer", "resolution=merge-duplicates").bodyValue(statsList).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> response.bodyToMono(String.class).flatMap(body -> {
                            System.err.println("Supabase error msg: " + body);
                            return Mono.error(new RuntimeException("Supabase API Error: " + body));
                        }))
                .toBodilessEntity().block();
    }

    // 유저 프로필에서 그룹 정보 저회
    public Map<String, Object> getUserProfile(String userId) {
        List<Map<String, Object>> response = getWebClient().get()
                .uri(uriBuilder -> uriBuilder.path("/rest/v1/user_profiles")
                        .queryParam(ProfileSchema.USER_ID, "eq." + userId).build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {}).block();

        return ((response != null) && !response.isEmpty()) ? response.get(0) : Map.of();
    }

    // 특정 그룹의 카테고리별 평균 데이터 가져오기
    public List<GroupAverageResponse> getGroupAverages(String ageGroup, String jobType,
            String period) {
        return getWebClient().get()
                .uri(uriBuilder -> uriBuilder.path("/rest/v1/group_averages")
                        .queryParam(ProfileSchema.AGE_GROUP, "eq." + ageGroup)
                        .queryParam(ProfileSchema.JOB_TYPE, "eq." + jobType)
                        .queryParam(StatsSchema.YEAR_MONTH, "eq." + period).build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GroupAverageResponse>>() {})
                .block();
    }
}
