// package com.PorTracker.PorTrackerBE.controller;

// import com.PorTracker.PorTrackerBE.dto.OnboardingRequest;
// import com.PorTracker.PorTrackerBE.service.OnboardingService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestHeader;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// @RestController
// @RequestMapping("/api/v1/onboarding")
// @RequiredArgsConstructor
// public class OnboardingController {
//     public final OnboardingService onboardingService;

//     @PostMapping // POST 처리
//     public String setupUser(
//             @RequestHeader("Authorization") String authHeader,
//             @RequestBody OnboardingRequest request) {
//         // "Bearer " 지우기
//         String accessToken = authHeader.replace("Bearer ", "");

//         return onboardingService.processOnboarding(
//                 accessToken, request.getUserId(), request.getUserEmail());
//     }
// }
