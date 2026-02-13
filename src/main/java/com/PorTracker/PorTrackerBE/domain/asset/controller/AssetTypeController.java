package com.PorTracker.PorTrackerBE.domain.asset.controller;

import com.PorTracker.PorTrackerBE.domain.asset.dto.AssetTypeRequest;
import com.PorTracker.PorTrackerBE.domain.asset.dto.AssetTypeResponse;
import com.PorTracker.PorTrackerBE.domain.asset.entity.AssetTypeRecord;
import com.PorTracker.PorTrackerBE.domain.asset.service.AssetTypeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/asset-types")
@RequiredArgsConstructor
public class AssetTypeController {
    private final AssetTypeService assetTypeService;

    @GetMapping
    public ResponseEntity<List<AssetTypeResponse>> getAssetTypes(
            @RequestHeader("X-USER-ID") String userId) {
        List<AssetTypeRecord> records = assetTypeService.getAllAssetTypes(userId);
        List<AssetTypeResponse> response = records.stream().map(AssetTypeResponse::from).toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> addAssetType(
            @RequestHeader("X-USER-ID") String userId, @RequestBody AssetTypeRequest request) {
        assetTypeService.addAssetType(userId, request);
        return ResponseEntity.ok().build();
    }
}
