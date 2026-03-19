package com.PorTracker.PorTrackerBE.domain.asset.controller;

import com.PorTracker.PorTrackerBE.domain.asset.dto.AssetTypeRequest;
import com.PorTracker.PorTrackerBE.domain.asset.dto.AssetTypeResponse;
import com.PorTracker.PorTrackerBE.domain.asset.entity.AssetTypeRecord;
import com.PorTracker.PorTrackerBE.domain.asset.service.AssetTypeService;
import com.PorTracker.PorTrackerBE.global.common.IdResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/asset-types")
@RequiredArgsConstructor
public class AssetTypeController {
    private final AssetTypeService assetTypeService;

    @GetMapping
    public ResponseEntity<List<AssetTypeResponse>> getAssetTypes(
            // @RequestHeader("X-USER-ID") String userId) {
            ) {
        // List<AssetTypeRecord> records = assetTypeService.getAllAssetTypes(userId);
        List<AssetTypeRecord> records = assetTypeService.getAllAssetTypes();
        List<AssetTypeResponse> response = records.stream().map(AssetTypeResponse::from).toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    // public ResponseEntity<Void> addAssetType(@RequestHeader("X-USER-ID") String userId,
    public ResponseEntity<IdResponse> addAssetType(@Valid @RequestBody AssetTypeRequest request) {
        // assetTypeService.addAssetType(userId, request);
        String publicId = assetTypeService.addAssetType(request);
        return ResponseEntity.ok(IdResponse.of(publicId));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{publicId}")
    // public ResponseEntity<Void> updateAssetType(@RequestHeader("X-USER-ID") String userId,
    public ResponseEntity<IdResponse> updateAssetType(
            @PathVariable("publicId") String publicId,
            @Valid @RequestBody AssetTypeRequest request) {
        // assetTypeService.updateAssetType(userId, publicId, request);
        assetTypeService.updateAssetType(publicId, request);
        return ResponseEntity.ok(IdResponse.of(publicId));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{publicId}")
    // public ResponseEntity<Void> deleteAssetType(@RequestHeader("X-USER-ID") String userId,
    public ResponseEntity<IdResponse> deleteAssetType(@PathVariable("publicId") String publicId) {
        // assetTypeService.deleteAssetType(userId, publicId);
        assetTypeService.deleteAssetType(publicId);
        return ResponseEntity.ok(IdResponse.of(publicId));
    }
}
