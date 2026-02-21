package com.PorTracker.PorTrackerBE.domain.target_portfolio.controller;

import com.PorTracker.PorTrackerBE.domain.target_portfolio.dto.TargetPortfolioCreateRequest;
import com.PorTracker.PorTrackerBE.domain.target_portfolio.dto.TargetPortfolioResponse;
import com.PorTracker.PorTrackerBE.domain.target_portfolio.dto.TargetPortfolioSnapshotUpdateRequest;
import com.PorTracker.PorTrackerBE.domain.target_portfolio.service.TargetPortfolioService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/target-portfolios")
@RequiredArgsConstructor
@Slf4j
public class TargetPortfolioController {
    private final TargetPortfolioService targetPortfolioService;

    @GetMapping
    public ResponseEntity<List<TargetPortfolioResponse>> getTargetPortfolios(
            // @RequestHeader("X-USER-ID") String userId) {
            ) {

        // Service에서 이미 모든 데이터가 조립되어 반환됨 (N+1 해결)
        // return ResponseEntity.ok(targetPortfolioService.getAllTargetPortfoliosFullData(userId)
        return ResponseEntity.ok(
                targetPortfolioService.getAllTargetPortfoliosFullData().stream()
                        .map(data -> TargetPortfolioResponse.from(data.portfolio(), data.items()))
                        .toList());
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<TargetPortfolioResponse> getTargetPortfolio(
            // @RequestHeader("X-USER-ID") String userId, @PathVariable("publicId") String publicId)
            // {
            @PathVariable("publicId") String publicId) {

        // Service에서 상세 데이터 조회 (예외 처리 포함)
        com.PorTracker.PorTrackerBE.domain.target_portfolio.dto.TargetPortfolioData data =
                // targetPortfolioService.getTargetPortfolioDetail(userId, publicId);
                targetPortfolioService.getTargetPortfolioDetail(publicId);

        return ResponseEntity.ok(TargetPortfolioResponse.from(data.portfolio(), data.items()));
    }

    @PostMapping
    public ResponseEntity<java.util.Map<String, String>> addTargetPortfolio(
            // @RequestHeader("X-USER-ID") String userId,
            @RequestBody TargetPortfolioCreateRequest request) {

        // String publicId = targetPortfolioService.addTargetPortfolio(userId, request);
        String publicId = targetPortfolioService.addTargetPortfolio(request);
        return ResponseEntity.ok(java.util.Map.of("id", publicId));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{publicId}")
    // public ResponseEntity<Void> updateTargetPortfolio(@RequestHeader("X-USER-ID") String userId,
    public ResponseEntity<Void> updateTargetPortfolio(
            @PathVariable("publicId") String publicId,
            @RequestBody TargetPortfolioCreateRequest request) {

        // targetPortfolioService.updateTargetPortfolio(userId, publicId, request);
        targetPortfolioService.updateTargetPortfolio(publicId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{publicId}/snapshots")
    // public ResponseEntity<Void> addSnapshot(@RequestHeader("X-USER-ID") String userId,
    public ResponseEntity<Void> addSnapshot(
            @PathVariable("publicId") String publicId,
            @RequestBody TargetPortfolioSnapshotUpdateRequest request) {

        // targetPortfolioService.addSnapshot(userId, publicId, request);
        targetPortfolioService.addSnapshot(publicId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{publicId}")
    // public ResponseEntity<Void> deleteTargetPortfolio(@RequestHeader("X-USER-ID") String userId,
    public ResponseEntity<Void> deleteTargetPortfolio(@PathVariable("publicId") String publicId) {

        // targetPortfolioService.deleteTargetPortfolio(userId, publicId);
        targetPortfolioService.deleteTargetPortfolio(publicId);
        return ResponseEntity.ok().build();
    }
}
