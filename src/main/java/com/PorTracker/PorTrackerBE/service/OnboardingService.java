package com.PorTracker.PorTrackerBE.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.PorTracker.PorTrackerBE.dto.UserSheetResopnse;

@Service
@Slf4j
@RequiredArgsConstructor
public class OnboardingService {
    private final GoogleDriveService googleDriveService;
    private final GoogleSheetService googleSheetService;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;


    public String processOnboarding(String accessToken, String userId, String userEmail) {
        // log.info("온보딩 시작 - 유저:{} 이메일:{}", userId, userEmail);
        // log.info("토큰: {}", accessToken);

        // test
        log.info("키: ", supabaseKey);

        try {

            WebClient webClient =
                    WebClient.builder().baseUrl(supabaseUrl).defaultHeader("apikey", supabaseKey)
                            .defaultHeader("Authorization", "Bearer " + supabaseKey).build();

            // List<Map<String, Object>> existingSheets = webClient.get()
            List<UserSheetResopnse> existingSheets = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/rest/v1/user_sheets")
                            .queryParam("user_id", "eq." + userId).build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<UserSheetResopnse>>() {})
                    .block();

            if (existingSheets != null && !existingSheets.isEmpty()) {
                String existingId = (String) existingSheets.get(0).spreadsheetId();
                log.info("이미 존재하는 시트 발견: {}", existingId);
                return "기존 시트를 사용합니다 - id: " + existingId;
            }


            String spreadsheetId = googleDriveService.createFinanceSheet(accessToken, userEmail);
            log.info("시트 생성! {}", spreadsheetId);

            googleSheetService.setupHeaders(accessToken, spreadsheetId);
            log.info("헤더 설정 완료");



            Map<String, String> data = Map.of("user_id", userId, "spreadsheet_id", spreadsheetId);

            webClient.post().uri("/rest/v1/user_sheets") // Supabase경로
                    .bodyValue(data).retrieve().toBodilessEntity().block(); // 동기로 처리

            log.info("수파베이스 저장 완료");
            return "온보딩 성공, 시트 id: " + spreadsheetId;

        } catch (Exception e) {
            log.error("온보딩 중 에러", e);
            return "실패: " + e.getMessage();
        }

    }
}
