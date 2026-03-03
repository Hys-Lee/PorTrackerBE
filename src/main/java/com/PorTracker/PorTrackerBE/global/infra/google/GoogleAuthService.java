package com.PorTracker.PorTrackerBE.global.infra.google;

import java.util.Map;

import org.checkerframework.checker.units.qual.t;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleAuthService {
    @Value("${google.client.id}") 
    private String clientId;

    @Value("${google.client.secret}") 
    private String clientSecret;

    private final RestTemplate restTemplate= new RestTemplate();

    public String refreshAccessToken(String refreshToken){
        String url = "https://oauth2.googleapis.com/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("refresh_token", refreshToken);
        params.add("grant_type", "refresh_token");
        
        try{
            Map<String, Object> response = restTemplate.postForObject(url, params, Map.class);
            return (String) response.get("access_token");
        }catch(HttpClientErrorException e){
            log.error("[GoogleAuth] Refresh failed. status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        }
        catch(Exception e){
            log.error("[GoogleAuth] Failed to refresh token", e.getMessage());
            return null;
        }
    }
}
