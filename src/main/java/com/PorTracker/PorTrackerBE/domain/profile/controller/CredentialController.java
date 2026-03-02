package com.PorTracker.PorTrackerBE.domain.profile.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PorTracker.PorTrackerBE.domain.profile.repository.CredentialRepository;
import com.PorTracker.PorTrackerBE.global.common.UserContextHolder;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/v1/credentials")
@RequiredArgsConstructor
public class CredentialController {
    private final CredentialRepository credentialRepository;

    @PostMapping("/google")
    public ResponseEntity<Void> updateToken(@RequestBody Map<String, String> body){
        String userId = UserContextHolder.getUserId();
        String googletoken = body.get("provider_token");

        credentialRepository.saveGoogleToken(UUID.fromString(userId), googletoken);
        return ResponseEntity.ok().build();
    }
    
}
