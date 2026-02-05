package com.PorTracker.PorTrackerBE.controller;

import com.PorTracker.PorTrackerBE.dto.ComparisonDto;
import com.PorTracker.PorTrackerBE.dto.TransactionDto;
import com.PorTracker.PorTrackerBE.service.FinanceService;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/v1/finance")
@RequiredArgsConstructor
@Validated // RequestParams 검증
public class FinanceController {
    private final FinanceService financeService;

    /** 내역 조회 GET /api/v1/finance/data?userId=~~&spreadsheetId=~~ */
    @GetMapping("/data")
    public List<TransactionDto> getFinanceData(
            @RequestHeader(value = "Authorization") @NotBlank(
                    message = "인증 토큰이 누락되었습니다.") String authHeader,

            @RequestParam(value = "userId") @NotBlank(message = "유저 ID는 필수입니다.") String userId,

            @RequestParam(value = "spreadsheetId") @NotBlank(
                    message = "시트 ID는 필수입니다.") String spreadsheetId) {

        String accessToken = authHeader.replace("Bearer ", "");

        return financeService.getAndContributeStats(accessToken, userId, spreadsheetId);
    }

    @GetMapping("/comparison")
    public List<ComparisonDto> getComparison(
            @RequestHeader("Authorization") @NotBlank(message = "인증 토큰이 누락되었습니다.") String auth,
            @RequestParam(value = "userId") @NotBlank(message = "유저 ID는 필수입니다.") String userId,
            @RequestParam(value = "spreadsheetId") @NotBlank(
                    message = "시트 ID는 필수입니다.") String spreadsheetId) {

        return financeService.getComparison(auth.replace("Bearer ", ""), userId, spreadsheetId);
    }
}
