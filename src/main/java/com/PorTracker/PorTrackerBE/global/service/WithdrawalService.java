package com.PorTracker.PorTrackerBE.global.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PorTracker.PorTrackerBE.domain.profile.repository.CredentialRepository;
import com.PorTracker.PorTrackerBE.global.constant.CentralSchema;
import com.PorTracker.PorTrackerBE.global.infra.google.GoogleDriveClient;
import com.PorTracker.PorTrackerBE.global.infra.sqlite.SqliteDatabaseManager;
import com.PorTracker.PorTrackerBE.global.infra.supabase.SupabaseAuthClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalService {
    private final CredentialRepository credentialRepository;
    private final GoogleDriveClient googleDriveClient;
    private final SqliteDatabaseManager sqliteManager;
    private final JdbcTemplate centralJdbcTemplate;
    private final SupabaseAuthClient supabaseAuthClient;

    @Transactional
    public void withdraw(String userId){
        UUID userUuid = UUID.fromString(userId);

        // 구글 드라이브 파일 삭제
        String accessToken = credentialRepository.getAccessToken(userUuid);
        if(accessToken != null){
            googleDriveClient.deleteFile(userId+".db", accessToken);
        }

        // 서버 로컬 파일 및 커넥션 처리
        sqliteManager.removeDataSource(userId);
        deleteLocalFiles(userId);

        // Supabase auth 계정 삭제.
        supabaseAuthClient.deleteUserAccount(userId);

        // centralJdbcTemplate.update(String.format("DELETE FROM public.credential WHERE %s = ?",CentralSchema.COL_ID), userUuid);
        // centralJdbcTemplate.update(String.format("DELETE FROM public.profile WHERE %s = ?",CentralSchema.COL_ID), userUuid);

        log.info("[Withdrawal] User {} has been successfully removed", userId);
    }

    private void deleteLocalFiles(String userId){
        try{
            Path path = Paths.get("db/"+userId+".db");
            Files.deleteIfExists(path);
            Files.deleteIfExists(Paths.get(path+"-wal"));
            Files.deleteIfExists(Paths.get(path+"-shm"));
            

        }catch(IOException e){
            log.error("[Withdrawal] Failed to delete local files", e);
        }
    }
}
