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

    public TagRecord getTagById(Long id) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);
        return tagRepository
                .findById(jdbcTemplate, id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA));
    }

    public List<TagRecord> getTagsByIds(List<Long> ids) {
        String userId = UserContextHolder.getUserId();
        org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate jdbcTemplate =
                sqliteManager.getNamedParameterJdbcTemplate(userId);

        return tagRepository.findByIds(jdbcTemplate, ids);
    }

    @Transactional
    public Long addTag(TagCreateRequest request) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        Long id = tagRepository.save(jdbcTemplate, request);
        log.info("Tag created successfully for user: {}, id: {}", userId, id);
        return id;
    }

    @Transactional
    public void updateTag(Long id, TagCreateRequest request) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        tagRepository
                .findById(jdbcTemplate, id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA));

        tagRepository.updateById(jdbcTemplate, id, request);
        log.info("Tag updated successfully for user: {}, id: {}", userId, id);
    }

    @Transactional
    public void deleteTag(Long id) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        tagRepository
                .findById(jdbcTemplate, id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA));

        tagRepository.deleteById(jdbcTemplate, id);
        log.info("Tag deleted successfully for user: {}, id: {}", userId, id);
    }
}
