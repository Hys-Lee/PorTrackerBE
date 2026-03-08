package com.PorTracker.PorTrackerBE.domain.memo.controller;

import com.PorTracker.PorTrackerBE.domain.memo.dto.MemoCreateRequest;
import com.PorTracker.PorTrackerBE.domain.memo.dto.MemoResponse;
import com.PorTracker.PorTrackerBE.domain.memo.entity.MemoRecord;
import com.PorTracker.PorTrackerBE.domain.memo.service.MemoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/memos")
@RequiredArgsConstructor
@Slf4j
public class MemoController {
    private final MemoService memoService;

    @GetMapping
    public ResponseEntity<List<MemoResponse>> getAllMemos(
            // @RequestHeader("X-USER-ID") String userId) {
            ) {
        // List<MemoRecord> records = memoService.getAllMemos(userId);
        List<MemoRecord> records = memoService.getAllMemos();
        List<MemoResponse> response = records.stream().map(MemoResponse::from).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<MemoResponse> getMemo(
            // @RequestHeader("X-USER-ID") String userId, @PathVariable("publicId") String publicId)
            // {
            @PathVariable("publicId") String publicId) {
        // MemoRecord record = memoService.getMemoById(userId, publicId);
        MemoRecord record = memoService.getMemoById(publicId);

        return ResponseEntity.ok(MemoResponse.from(record));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "다건 조회", description = "여러 publicId 받아 리스트로 반환 - 순서는 랜덤")
    @GetMapping("/bulk")
    public ResponseEntity<List<MemoResponse>> getMemosBulk(
            @io.swagger.v3.oas.annotations.Parameter(description = "조회할 publicId 리스트 (쉼표로 구분)")
            @org.springframework.web.bind.annotation.RequestParam List<String> publicIds) {
        
        List<MemoRecord> records = memoService.getMemoByPublicIds(publicIds);
        List<MemoResponse> response = records.stream().map(MemoResponse::from).toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<java.util.Map<String, String>> addMemo(
            // @RequestHeader("X-USER-ID") String userId, @RequestBody MemoCreateRequest request) {
            @Valid @RequestBody MemoCreateRequest request) {
        // String publicId = memoService.addMemo(userId, request);
        String publicId = memoService.addMemo(request);
        return ResponseEntity.ok(java.util.Map.of("id", publicId));
    }

    @PutMapping("/{publicId}")
    public ResponseEntity<Void> updateMemo(
            // @RequestHeader("X-USER-ID") String userId,
            @PathVariable("publicId") String publicId,
            @Valid @RequestBody MemoCreateRequest request) {
        // memoService.updateMemo(userId, publicId, request);
        memoService.updateMemo(publicId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> deleteMemo(
            // @RequestHeader("X-USER-ID") String userId, @PathVariable("publicId") String publicId)
            // {
            @PathVariable("publicId") String publicId) {
        // memoService.deleteMemo(userId, publicId);
        memoService.deleteMemo(publicId);
        return ResponseEntity.ok().build();
    }
}
