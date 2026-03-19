package com.PorTracker.PorTrackerBE.domain.tag.service;

import com.PorTracker.PorTrackerBE.domain.tag.dto.TagCreateRequest;
import com.PorTracker.PorTrackerBE.domain.tag.entity.TagRecord;
import com.PorTracker.PorTrackerBE.domain.tag.repository.TagRepository;
import com.PorTracker.PorTrackerBE.global.common.UserContextHolder;
import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import com.PorTracker.PorTrackerBE.global.infra.sqlite.SqliteDatabaseManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {
    private final SqliteDatabaseManager sqliteManager;
    private final TagRepository tagRepository;

    public List<TagRecord> getAllTags() {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);
        return tagRepository.findAll(jdbcTemplate);
    }

    public TagRecord getTagById(String publicId) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);
        return tagRepository
                .findByPublicId(jdbcTemplate, publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA));
    }

    public List<TagRecord> getTagsByIds(List<String> publicIds) {
        String userId = UserContextHolder.getUserId();
        org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate jdbcTemplate =
                sqliteManager.getNamedParameterJdbcTemplate(userId);

        return tagRepository.findByPublicIds(jdbcTemplate, publicIds);
    }

    @Transactional
    public String addTag(TagCreateRequest request) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        String publicId = java.util.UUID.randomUUID().toString();
        tagRepository.save(jdbcTemplate, request, publicId);
        log.info("Tag created successfully for user: {}, publicId: {}", userId, publicId);
        return publicId;
    }

    @Transactional
    public void updateTag(String publicId, TagCreateRequest request) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        tagRepository
                .findByPublicId(jdbcTemplate, publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA));

        tagRepository.updateByPublicId(jdbcTemplate, publicId, request);
        log.info("Tag updated successfully for user: {}, publicId: {}", userId, publicId);
    }

    @Transactional
    public void deleteTag(String publicId) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        tagRepository
                .findByPublicId(jdbcTemplate, publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA));

        tagRepository.deleteByPublicId(jdbcTemplate, publicId);
        log.info("Tag deleted successfully for user: {}, publicId: {}", userId, publicId);
    }
}
