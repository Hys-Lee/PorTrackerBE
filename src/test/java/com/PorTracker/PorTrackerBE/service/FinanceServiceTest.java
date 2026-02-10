package com.PorTracker.PorTrackerBE.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.PorTracker.PorTrackerBE.constant.ProfileSchema;
import com.PorTracker.PorTrackerBE.dto.TransactionDto;
import com.PorTracker.PorTrackerBE.repository.SupabaseRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class) // 가짜객체(Mockito) 사용
class FinanceServiceTest {

    @Mock private GoogleSheetService googleSheetService;

    @Mock private StatisticsService statisticsService;

    @Mock private SupabaseRepository supabaseRepository;

    @Mock private RedisTemplate<String, Object> redisTemplate;

    @Mock // 내부 동작 관련
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks // 위 mocks들 실제 주입받는 실제 서비스
    private FinanceService financeService;

    @Test
    @DisplayName("캐시 없을 때 구글 시트에서 데이터 가져와 익명화 후 저장하는지")
    void getAndContributeStats_CacheMiss_Success() throws Exception {
        // 준비
        String accessToken = "test-token";
        String userId = "user-id";
        String sheetId = "sheet-id";
        List<TransactionDto> mockData =
                List.of(new TransactionDto("2026-02-05", "food", "lunch", 10000L, ""));

        Map<String, Object> mockProfile =
                Map.of(ProfileSchema.AGE_GROUP, "30s", ProfileSchema.JOB_TYPE, "developer");

        // 레디스 캐시 없을 때
        given(redisTemplate.opsForValue()).willReturn(valueOperations); // 왜 given이나
        // willReturn
        // 자돵완성이 안되냐?
        given(valueOperations.get(anyString())).willReturn(null); // 왜 given이나 willReturn
        // 자돵완성이 안되냐?

        // 구글 서비스 mocking
        given(googleSheetService.getTransactions(eq(accessToken), eq(sheetId), anyString()))
                .willReturn(mockData);

        // supabase 프로필 조회 mocking
        given(supabaseRepository.getUserProfile(userId)).willReturn(mockProfile);

        // 모킹 실행
        List<TransactionDto> result =
                financeService.getAndContributeStats(accessToken, userId, sheetId, false);

        // validation
        assertEquals(1, result.size());
        assertEquals("food", result.get(0).category()); // 카테고리 food인지

        // 내부 로직 확인
        verify(statisticsService, times(1)).anonymized(eq(userId), anyList(), eq(mockProfile));
        verify(statisticsService, times(1)).contributeStats(any());
    }

    @Test
    @DisplayName("데이터 조회 시 통계 기여 로직이 비동기 호출되어야")
    void getAndContributeStats_AsyncVerification() throws Exception {
        // 준비
        String accessToekn = "test-token";
        String userId = "user-1";
        String sheetId = "sheet-1";

        List<TransactionDto> mockData =
                List.of(new TransactionDto("2026-02-05", "food", "lunch", 10000L, ""));

        Map<String, Object> mockProfile =
                Map.of(ProfileSchema.AGE_GROUP, "30s", ProfileSchema.JOB_TYPE, "developer");

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(anyString())).willReturn(null);
        given(googleSheetService.getTransactions(eq(accessToekn), eq(sheetId), anyString()))
                .willReturn(mockData);
        given(supabaseRepository.getUserProfile(userId)).willReturn(mockProfile);

        // 실행
        financeService.getAndContributeStats(accessToekn, userId, sheetId, false);

        // 검증
        verify(statisticsService, timeout(1000).times(1)).contributeStats(anyList());
    }
}
