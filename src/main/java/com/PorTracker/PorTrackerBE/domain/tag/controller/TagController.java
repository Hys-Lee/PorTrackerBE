package com.PorTracker.PorTrackerBE.domain.tag.controller;

import com.PorTracker.PorTrackerBE.domain.tag.dto.TagCreateRequest;
import com.PorTracker.PorTrackerBE.domain.tag.dto.TagResponse;
import com.PorTracker.PorTrackerBE.domain.tag.entity.TagRecord;
import com.PorTracker.PorTrackerBE.domain.tag.service.TagService;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tag", description = "태그 관리 API")
public class TagController {

    private final TagService tagService;

    @Operation(summary = "모든 태그 조회")
    @GetMapping
    public ResponseEntity<List<TagResponse>> getAllTags() {
        List<TagRecord> records = tagService.getAllTags();
        List<TagResponse> response = records.stream().map(TagResponse::from).toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "특정 태그 조회")
    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getTag(@PathVariable("id") Long id) {
        TagRecord record = tagService.getTagById(id);
        return ResponseEntity.ok(TagResponse.from(record));
    }

    @Operation(summary = "다건 조회", description = "여러 id 받아 리스트로 반환")
    @GetMapping("/bulk")
    public ResponseEntity<List<TagResponse>> getTagsBulk(
            @io.swagger.v3.oas.annotations.Parameter(description = "조회할 id 리스트 (쉼표로 구분)")
            @org.springframework.web.bind.annotation.RequestParam List<Long> ids) {
        List<TagRecord> records = tagService.getTagsByIds(ids);
        List<TagResponse> response = records.stream().map(TagResponse::from).toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "새 태그 생성")
    @PostMapping
    public ResponseEntity<java.util.Map<String, Long>> addTag(
            @Valid @RequestBody TagCreateRequest request) {
        Long id = tagService.addTag(request);
        return ResponseEntity.ok(java.util.Map.of("id", id));
    }

    @Operation(summary = "태그 수정")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateTag(
            @PathVariable("id") Long id,
            @Valid @RequestBody TagCreateRequest request) {
        tagService.updateTag(id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "태그 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable("id") Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok().build();
    }
}
