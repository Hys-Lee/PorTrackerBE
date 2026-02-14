package com.PorTracker.PorTrackerBE.domain.memo.controller;

import com.PorTracker.PorTrackerBE.domain.memo.dto.MemoCreateRequest;
import com.PorTracker.PorTrackerBE.domain.memo.dto.MemoResponse;
import com.PorTracker.PorTrackerBE.domain.memo.entity.MemoRecord;
import com.PorTracker.PorTrackerBE.domain.memo.service.MemoService;
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
@RequestMapping("/api/v1/memos")
@RequiredArgsConstructor
@Slf4j
public class MemoController {
    private final MemoService memoService;

    @GetMapping
    public ResponseEntity<List<MemoResponse>> getMemos(@RequestHeader("X-USER-ID") String userId) {
        List<MemoRecord> records = memoService.getAllMemos(userId);
        List<MemoResponse> response = records.stream().map(MemoResponse::from).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<MemoResponse> getMemo(
            @RequestHeader("X-USER-ID") String userId, @PathVariable String publicId) {
        MemoRecord record = memoService.getMemoById(userId, publicId);

        if (record == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(MemoResponse.from(record));
    }

    @PostMapping
    public ResponseEntity<Void> addMemo(
            @RequestHeader("X-USER-ID") String userId, @RequestBody MemoCreateRequest request) {

        memoService.addMemo(userId, request);
        return ResponseEntity.ok().build();
    }
}
