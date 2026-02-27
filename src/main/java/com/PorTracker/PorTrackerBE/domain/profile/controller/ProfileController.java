package com.PorTracker.PorTrackerBE.domain.profile.controller;

import com.PorTracker.PorTrackerBE.domain.profile.dto.ProfileResponse;
import com.PorTracker.PorTrackerBE.domain.profile.dto.ProfileUpdateRequest;
import com.PorTracker.PorTrackerBE.domain.profile.entity.ProfileRecord;
import com.PorTracker.PorTrackerBE.domain.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final ProfileService profileService;

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
}
