package com.PorTracker.PorTrackerBE.domain.asset.controller;

import com.PorTracker.PorTrackerBE.domain.asset.dto.AssetCreateRequest;
import com.PorTracker.PorTrackerBE.domain.asset.dto.AssetResponse;
import com.PorTracker.PorTrackerBE.domain.asset.entity.AssetRecord;
import com.PorTracker.PorTrackerBE.domain.asset.service.AssetService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@Slf4j
public class AssetController {
    private final AssetService assetService;

    @GetMapping
    public ResponseEntity<List<AssetResponse>> getAssets(
            @RequestHeader("X-USER-ID") String userId) {
        List<AssetRecord> records = assetService.getAllAssets(userId);
        List<AssetResponse> response = records.stream().map(AssetResponse::from).toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> addAsset(@RequestHeader("X-USER-ID") String userId,
            @RequestBody AssetCreateRequest request) {

        assetService.addAsset(userId, request);
        return ResponseEntity.ok().build();
    }
}
