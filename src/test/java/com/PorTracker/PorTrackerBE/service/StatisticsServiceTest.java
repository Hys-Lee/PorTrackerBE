package com.PorTracker.PorTrackerBE.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.PorTracker.PorTrackerBE.repository.SupabaseRepository;
import com.PorTracker.PorTrackerBE.dto.AnonymizedStatsDto;
import com.PorTracker.PorTrackerBE.dto.TransactionDto;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private SupabaseRepository supabaseRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    @DisplayName("카테고리별 금액 합산 및 익명화 롲릭 검증")
    void anonymized_Success() {
        // 준비 (given)
        String userId = "user-id-mock";

        Map<String, Object> profile = Map.of("age_group", "30s", "job_type", "developer");

        List<TransactionDto> transactions =
                List.of(new TransactionDto("2026-02-05", "식비", "점심", 10000L, ""),
                        new TransactionDto("2026-02-06", "식비", "저녁", 20000L, ""),
                        new TransactionDto("2026-02-07", "교통", "택시", 15000L, ""));


        // 실행
        List<AnonymizedStatsDto> result =
                statisticsService.anonymized(userId, transactions, profile);

        // 검증

        assertEquals(2, result.size()); // 식비,교통


        AnonymizedStatsDto foodStats =
                result.stream().filter(s -> s.category().equals("식비")).findFirst().orElseThrow();
        assertNotEquals(userId, foodStats.hashedId(), "유저 ID가 익명화되지 않았슴다.");
        assertNotNull(foodStats.hashedId(), "해시 ID가 생성되지 않았슴다.");
        assertEquals("30s", foodStats.ageGroup(), "나이 그룹이 일치하지 않슴다.");
        assertEquals("developer", foodStats.jobType(), "직업 유형이 일치하지 않슴다.");

    }


}
