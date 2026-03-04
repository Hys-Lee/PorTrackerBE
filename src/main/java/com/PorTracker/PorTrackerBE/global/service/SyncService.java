package com.PorTracker.PorTrackerBE.global.service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.checkerframework.checker.units.qual.t;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.PorTracker.PorTrackerBE.domain.profile.entity.CredentialRecord;
import com.PorTracker.PorTrackerBE.domain.profile.repository.CredentialRepository;
import com.PorTracker.PorTrackerBE.domain.statistic.service.StatisticService;
import com.PorTracker.PorTrackerBE.global.infra.google.GoogleAuthService;
import com.PorTracker.PorTrackerBE.global.infra.google.GoogleDriveClient;
// import com.PorTracker.PorTrackerBE.global.infra.supabase.SupabaseAuthClient;
import com.PorTracker.PorTrackerBE.global.infra.sqlite.SqliteDatabaseManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {
    // private final SupabaseAuthClient supabaseAuthClient;
    private final CredentialRepository credentialRepository;
    private final GoogleDriveClient googleDriveClient;
    private final GoogleAuthService googleAuthService;
    private final SqliteDatabaseManager sqliteManager;
    private final StatisticService statisticService;
    


    public void downloadFromCloud (String userId){
        String accessToken = credentialRepository.getAccessToken(UUID.fromString(userId));
        if(accessToken==null) return;

        String fileId = googleDriveClient.findFileIdByName(userId+".db", accessToken);
        if(fileId!=null){
            byte[] content = googleDriveClient.downloadFile(fileId, accessToken);

            if(content!=null){
                try{
                    // 파일 새로 받았기 때문에, 기존 메모리에 있던 datasource연결 끊기...
                    sqliteManager.removeDataSource(userId);

                    Files.write(Paths.get("db/"+userId+".db"), content);
                    log.info("[Sync] Downloaded database form cloud for user: {}",userId);
                    
                }catch(Exception e){
                    log.error("[Sync] File write failed",e);
                }
            }
        }

    }

    
    public void uploadToCloud (String userId){
        // 업로드 전 wal-> db파일로 데이터 병합하기
        try{
            sqliteManager.getJdbcTemplate(userId).execute("PRAGMA wal_checkpoint(FULL)");
            log.info("[Sync] WAL Checkpoint completed for user: {}", userId);

        }catch(Exception e){
            log.warn("[Sync] Checkpoint failed, but upload anyway: {}",e.getMessage());
        }
    

        // db에서 토큰 가져오기
        CredentialRecord cred = credentialRepository.findByUserId(UUID.fromString(userId)).orElse(null);

        if(cred == null | cred.getAccessToken() == null){
            log.error("[Sync] No Credentials for user: {}",userId);
            return;
        }


        // 업로드 시도 (401시 토큰 갱신)
        try{
            googleDriveClient.syncToDrive(userId, cred.getAccessToken());
            log.info("[Sync] Upload successful for user: {}", userId);

        }catch(HttpClientErrorException e){

            if(e.getStatusCode() == HttpStatus.UNAUTHORIZED){
                log.info("[Sync] Token expired, refresing for user: {}", userId);

                String newAccessToken = googleAuthService.refreshAccessToken(cred.getRefreshToken());

                if (newAccessToken != null){
                    credentialRepository.updateAccessToken(UUID.fromString(userId), newAccessToken);
                    googleDriveClient.syncToDrive(userId, newAccessToken);
                    log.info("[Sync] Uploaded successfully after token refreshed");
                }
            }else{
                log.error("[Sync] Google API Client Error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            }
        } catch(Exception e){
            log.error("[Sync] Unexpected eror during upload: {}", e.getMessage());
        }


        try{
            statisticService.contributeTotalAsset(userId);
        }catch(Exception e){
            log.warn("[Stat] Failed to contribute statistics: {}",e.getMessage());
        }

        // String accessToken = supabaseAuthClient.getGoogleAccessToken(userId);
        // String accessToken = credentialRepository.getAccessToken(UUID.fromString(userId));
        // if(accessToken==null){
        //     log.error("[Sync] No google aceess token found for user: {}", userId);
        //     return;
        // }
        // googleDriveClient.syncToDrive(userId, accessToken);
    }

}
