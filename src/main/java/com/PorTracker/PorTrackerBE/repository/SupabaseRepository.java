package com.PorTracker.PorTrackerBE.repository;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import com.PorTracker.PorTrackerBE.constant.ProfileSchema;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SupabaseRepository {
    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;


    // 공용 WebClient 빌더
    private WebClient getWebClient() {
        return WebClient.builder().baseUrl(supabaseUrl).defaultHeader("apikey", supabaseKey)
                .defaultHeader("Authorization", "Bearer " + supabaseKey)
                .defaultHeader("Content-Type", "application/json").build();
    }

    // 통계 데이터 upsert
    public void upsertCategoryStats(List<?> statsList) {
        getWebClient().post().uri("/rest/v1/category_stats")
                .header("Prefer", "resolution=merge-duplicates").bodyValue(statsList).retrieve()
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

}
