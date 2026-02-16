package com.PorTracker.PorTrackerBE.domain.actual_portfolio.controller;

import com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto.ActualPortfolioCreateRequest;
import com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto.ActualPortfolioResponse;
// import com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto.ActualPortfolioTransactionRequest;
import com.PorTracker.PorTrackerBE.domain.actual_portfolio.entity.ActualPortfolioRecord;
import com.PorTracker.PorTrackerBE.domain.actual_portfolio.service.ActualPortfolioService;
// import
// com.PorTracker.PorTrackerBE.domain.actual_portfolio.service.ActualPortfolioTransactionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/actual-portfolios")
@RequiredArgsConstructor
@Slf4j
public class ActualPortfolioController {
    private final ActualPortfolioService actualPortfolioService;

    // private final ActualPortfolioTransactionService actualPortfolioTransactionService;

    @GetMapping
    public ResponseEntity<List<ActualPortfolioResponse>> getActualPortfolios(
            @RequestHeader("X-USER-ID") String userId) {
        List<ActualPortfolioRecord> records = actualPortfolioService.getAllActualPortfolios(userId);
        List<ActualPortfolioResponse> response =
                records.stream().map(ActualPortfolioResponse::from).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<ActualPortfolioResponse> getActualPortfolio(
            @RequestHeader("X-USER-ID") String userId, @PathVariable("publicId") String publicId) {
        ActualPortfolioRecord record =
                actualPortfolioService.getActualPortfolioById(userId, publicId);

        if (record == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ActualPortfolioResponse.from(record));
    }

    @PostMapping
    public ResponseEntity<Void> addActualPortfolio(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody ActualPortfolioCreateRequest request) {

        actualPortfolioService.addActualPortfolio(userId, request);
        return ResponseEntity.ok().build();
    }

    // ===== 기존 코드 (주석 처리) =====
    // @PostMapping("/transaction")
    // public ResponseEntity<String> addTransaction(
    // @RequestHeader("X-USER-ID") String userId,
    // @RequestBody ActualPortfolioTransactionRequest request) {
    //
    // actualPortfolioTransactionService.insertTransaction(userId, request);
    //
    // return ResponseEntity.ok("Transaction Insert succuess!");
    // }
}
