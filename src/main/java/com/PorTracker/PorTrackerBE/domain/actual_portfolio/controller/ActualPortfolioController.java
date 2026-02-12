package com.PorTracker.PorTrackerBE.domain.actual_portfolio.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto.ActualPortfolioTransactionRequest;
import com.PorTracker.PorTrackerBE.domain.actual_portfolio.service.ActualPortfolioTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
@RequestMapping("/api/v1/portfolio/actual")
@RequiredArgsConstructor
public class ActualPortfolioController {
    private final ActualPortfolioTransactionService actualPortfolioTransactionService;

    @PostMapping("/transaction")
    public ResponseEntity<String> addTransaction(@RequestHeader("X-USER-ID") String userId,
            @RequestBody ActualPortfolioTransactionRequest request) {

        actualPortfolioTransactionService.insertTransaction(userId, request);

        return ResponseEntity.ok("Transaction Insert succuess!");
    }

}
