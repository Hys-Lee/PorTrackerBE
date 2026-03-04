package com.PorTracker.PorTrackerBE.domain.profile.controller;

import com.PorTracker.PorTrackerBE.domain.profile.dto.ProfileResponse;
import com.PorTracker.PorTrackerBE.domain.profile.dto.ProfileUpdateRequest;
import com.PorTracker.PorTrackerBE.domain.profile.entity.ProfileRecord;
import com.PorTracker.PorTrackerBE.domain.profile.service.ProfileService;
import com.PorTracker.PorTrackerBE.global.common.UserContextHolder;
import com.PorTracker.PorTrackerBE.global.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final WithdrawalService withdrawalService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile() {
        ProfileRecord record = profileService.getMyProfile();
        return ResponseEntity.ok(ProfileResponse.from(record));
    }

    @PatchMapping("/me")
    public ResponseEntity<Void> updateMyProfile(@RequestBody ProfileUpdateRequest request) {
        profileService.updateMyProfile(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw() {
        String userId = UserContextHolder.getUserId();
        withdrawalService.withdraw(userId);
        return ResponseEntity.noContent().build();
    }
}
