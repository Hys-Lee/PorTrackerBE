package com.PorTracker.PorTrackerBE.global.infra.supabase;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupabaseAuthClient {
    
    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service.role.key}") // service_role 키여야 한다는데?
    private String serviceRoleKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getGoogleRefreshToken(String userId){
        String url = supabaseUrl+"/auth/v1/admin/users/"+userId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization","Bearer "+serviceRoleKey);
        headers.set("apikey", serviceRoleKey);

        try{
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers),Map.class);

            if(response.getStatusCode() == HttpStatus.OK){
                Map<String,Object> body = response.getBody();

                // test
                log.info("[Supabase] response body: {}", body);

                // supabase응답 구조에 따라서.
                String googleRefreshToken =  (String) body.get("provider_refresh_token");
                if(googleRefreshToken==null){
                    log.warn("[Supabase] provider_refresh_token is null for user: {}", userId);
                }
                return googleRefreshToken;
            }
        }catch(Exception e){
            log.error("[Supabase] Failed to fetch Google Token:{} for user: {}",e.getMessage(),userId,e);
        }
        return null;
    }


    public void deleteUserAccount (String userId){
        String url = supabaseUrl+"/auth/v1/admin/users/"+userId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer "+serviceRoleKey);
        headers.set("apikey", serviceRoleKey);

        try{
            restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
            log.info("[Supabase] Successfully deleted auth account for user: {}", userId);
        }catch(Exception e){
            log.error("[Supabase] Failed to delete auth account for user: {}", userId, e.getMessage());
            throw new BusinessException(ErrorCode.DATA_CREATE_FAILED);
        }
    }

}
