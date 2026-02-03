package com.PorTracker.PorTrackerBE.controller;

import com.PorTracker.PorTrackerBE.dto.ComparisonDto;
import com.PorTracker.PorTrackerBE.dto.TransactionDto;
import com.PorTracker.PorTrackerBE.service.FinanceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/finance")
@RequiredArgsConstructor
public class FinanceController {
    private final FinanceService financeService;

    /** 내역 조회 GET /api/v1/finance/data?userId=~~&spreadsheetId=~~ */
    @GetMapping("/data")
    public List<TransactionDto> getFinanceData(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestParam(value = "userId") String userId,
            @RequestParam(value = "spreadsheetId") String spreadsheetId) {
        String accessToken = authHeader.replace("Bearer ", "");

        return financeService.getAndContributeStats(accessToken, userId, spreadsheetId);
    }

    @GetMapping("/comparison")
    public List<ComparisonDto> getComparison(@RequestHeader("Authorization") String auth,
            @RequestParam(value = "userId") String userId,
            @RequestParam(value = "spreadsheetId") String spreadsheetId) {

        return financeService.getComparison(auth.replace("Bearer ", ""), userId, spreadsheetId);
    }
}
