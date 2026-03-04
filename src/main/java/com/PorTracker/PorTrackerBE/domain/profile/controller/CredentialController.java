package com.PorTracker.PorTrackerBE.domain.profile.controller;

import com.PorTracker.PorTrackerBE.domain.profile.repository.CredentialRepository;
import com.PorTracker.PorTrackerBE.global.common.UserContextHolder;
import com.PorTracker.PorTrackerBE.global.infra.supabase.SupabaseAuthClient;
import com.PorTracker.PorTrackerBE.global.service.SyncService;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/credentials")
@RequiredArgsConstructor
public class CredentialController {
    private final CredentialRepository credentialRepository;
    private final SupabaseAuthClient supabaseAuthClient;
    private final SyncService syncService;

    @PostMapping("/google")
    public ResponseEntity<Void> updateToken(@RequestBody Map<String, String> body) {
        String userId = UserContextHolder.getUserId();
        String googletoken = body.get("provider_token");
        // String refreshToken = body.get("refresh_token");

        String googleRefreshToken = body.get("provider_refresh_token");
        if (googleRefreshToken == null) {

            googleRefreshToken = supabaseAuthClient.getGoogleRefreshToken(userId);
        }

        credentialRepository.saveGoogleToken(
                UUID.fromString(userId), googletoken, googleRefreshToken);

        // 즉시 빈 db라도 클라우드에 백업 생성
        syncService.uploadToCloud(userId);

        return ResponseEntity.ok().build();
    }
}
