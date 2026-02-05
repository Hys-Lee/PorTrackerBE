package com.PorTracker.PorTrackerBE.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import com.PorTracker.PorTrackerBE.service.FinanceService;


import static org.mockito.ArgumentMatcher.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


@WebMvcTest(FinanceController.class)
class FinanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FinanceService financeService;

    @Test
    @DisplayName("필수 파라미터인 userId가 누락되면 400 error")
    void getFinanceData_ValidationFailed() throws Exception {
        mockMvc.perform(get("/api/v1/finance/data").header("Authorization", "Bearer test-token")
                .param("spreadsheetId", "some-id")).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()));
    }

    @Test
    @DisplayName("BusinessException 발생 시 규격화된 에러 응답 반환")
    void getFinanceData_BusinessException() throws Exception {
        // 준비 - 설정하기
        given(financeService.getAndContributeStats(anyString(), anyString(), anyString(),
                anyBoolean())).willThrow(new BusinessException(ErrorCode.SHEET_NOT_FOUND));

        // 실행, 검증
        mockMvc.perform(get("/api/v1/finance/data").header("Authorization", "Bearer test-token")
                .param("userId", "user-1").param("spreadsheetId", "sheet-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.SHEET_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.SHEET_NOT_FOUND.getMessage()));



    }
}
