package com.PorTracker.PorTrackerBE.domain.actual_portfolio.controller;

import com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto.ActualPortfolioCreateRequest;
import com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto.ActualPortfolioResponse;
import com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto.ActualPortfolioSearchRequest;
import com.PorTracker.PorTrackerBE.domain.actual_portfolio.entity.ActualPortfolioRecord;
import com.PorTracker.PorTrackerBE.domain.actual_portfolio.service.ActualPortfolioService;
import com.PorTracker.PorTrackerBE.global.common.IdResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
            // @RequestHeader("X-USER-ID") String userId) {
            ) {
        // List<ActualPortfolioRecord> records =
        // actualPortfolioService.getAllActualPortfolios(userId);
        List<ActualPortfolioRecord> records = actualPortfolioService.getAllActualPortfolios();
        List<ActualPortfolioResponse> response =
                records.stream().map(ActualPortfolioResponse::from).toList();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Unlinked actual portfolios",
            description = "Get actual portfolios that are not linked to any memo")
    @GetMapping("/unlinked")
    public ResponseEntity<List<ActualPortfolioResponse>> getUnlinkedActualPortfolios() {
        List<ActualPortfolioRecord> records = actualPortfolioService.getUnlinkedActualPortfolios();
        List<ActualPortfolioResponse> response =
                records.stream().map(ActualPortfolioResponse::from).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<ActualPortfolioResponse> getActualPortfolio(
            // @RequestHeader("X-USER-ID") String userId, @PathVariable("publicId") String publicId)
            // {
            @PathVariable("publicId") String publicId) {
        ActualPortfolioRecord record =
                // actualPortfolioService.getActualPortfolioById(userId, publicId);
                actualPortfolioService.getActualPortfolioById(publicId);

        if (record == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ActualPortfolioResponse.from(record));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "다건 조회",
            description = "여러 publicId 받아 리스트로 반환 - 순서는 랜덤")
    @GetMapping("/bulk")
    public ResponseEntity<List<ActualPortfolioResponse>> getActualPortfoliosBulk(
            @io.swagger.v3.oas.annotations.Parameter(description = "조회할 publicId 리스트 (쉼표로 구분)")
                    @org.springframework.web.bind.annotation.RequestParam
                    List<String> publicIds) {

        List<ActualPortfolioRecord> records =
                actualPortfolioService.getActualPortfolioByPublicIds(publicIds);
        List<ActualPortfolioResponse> response =
                records.stream().map(ActualPortfolioResponse::from).toList();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "포트폴리오 검색 및 필터링", description = "자산, 통화, 거래타입, 기간별 필터링 및 최근 내역 조회를 수행합니다.")
    @GetMapping("/search")
    public ResponseEntity<List<ActualPortfolioResponse>> searchActualPortfolios(
            @io.swagger.v3.oas.annotations.Parameter(description = "조회할 publicId 리스트 (쉼표로 구분)")
                    @ParameterObject
                    ActualPortfolioSearchRequest request) {

        List<ActualPortfolioRecord> records = actualPortfolioService.search(request);
        List<ActualPortfolioResponse> response =
                records.stream().map(ActualPortfolioResponse::from).toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/with-memo")
    public ResponseEntity<IdResponse> addActualPortfolioWithMemo(
            @Valid @RequestBody com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto.ActualPortfolioWithMemoCreateRequest request) {

        String publicId = actualPortfolioService.addActualPortfolioWithMemo(request);
        return ResponseEntity.ok(IdResponse.of(publicId));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{publicId}/with-memo")
    public ResponseEntity<IdResponse> updateActualPortfolioWithMemo(
            @PathVariable("publicId") String publicId,
            @Valid @RequestBody com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto.ActualPortfolioWithMemoCreateRequest request) {

        actualPortfolioService.updateActualPortfolioWithMemo(publicId, request);
        return ResponseEntity.ok(IdResponse.of(publicId));
    }

    @PostMapping
    // public ResponseEntity<Void> addActualPortfolio(@RequestHeader("X-USER-ID") String userId,
    public ResponseEntity<IdResponse> addActualPortfolio(
            @Valid @RequestBody ActualPortfolioCreateRequest request) {

        // actualPortfolioService.addActualPortfolio(userId, request);
        String publicId = actualPortfolioService.addActualPortfolio(request);
        return ResponseEntity.ok(IdResponse.of(publicId));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{publicId}")
    // public ResponseEntity<Void> updateActualPortfolio(@RequestHeader("X-USER-ID") String userId,
    public ResponseEntity<IdResponse> updateActualPortfolio(
            @PathVariable("publicId") String publicId,
            @Valid @RequestBody ActualPortfolioCreateRequest request) {

        // actualPortfolioService.updateActualPortfolio(userId, publicId, request);
        actualPortfolioService.updateActualPortfolio(publicId, request);
        return ResponseEntity.ok(IdResponse.of(publicId));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{publicId}")
    // public ResponseEntity<Void> deleteActualPortfolio(@RequestHeader("X-USER-ID") String userId,
    public ResponseEntity<IdResponse> deleteActualPortfolio(
            @PathVariable("publicId") String publicId) {

        // actualPortfolioService.deleteActualPortfolio(userId, publicId);
        actualPortfolioService.deleteActualPortfolio(publicId);
        return ResponseEntity.ok(IdResponse.of(publicId));
    }
}
