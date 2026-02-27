package com.PorTracker.PorTrackerBE.domain.profile.service;

import com.PorTracker.PorTrackerBE.domain.profile.dto.ProfileUpdateRequest;
import com.PorTracker.PorTrackerBE.domain.profile.entity.ProfileRecord;
import com.PorTracker.PorTrackerBE.domain.profile.repository.ProfileRepository;
import com.PorTracker.PorTrackerBE.global.common.UserContextHolder;
import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileRecord getMyProfile() {
        // 1. Context에서 userId(String)를 꺼내서 UUID로 변환 (Supabase PK 규격)
        String userIdStr = UserContextHolder.getUserId();
        UUID userId = UUID.fromString(userIdStr);

        // 2. 조회 및 예외 처리 (기존 스타일 적용)
        return profileRepository
                .findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA));
    }

    @Transactional
    public void updateMyProfile(ProfileUpdateRequest request) {
        String userIdStr = UserContextHolder.getUserId();
        UUID userId = UUID.fromString(userIdStr);

        // 1. Check exists
        profileRepository
                .findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_DATA));

        // 2. Update
        profileRepository.updateProfile(userId, request.getNickname(), request.getBaseCurrencyId());

        log.info("profile updated successfully for user: {}", userIdStr);
    }
}
