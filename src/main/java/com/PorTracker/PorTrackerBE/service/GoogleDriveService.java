package com.PorTracker.PorTrackerBE.service;

import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleDriveService {

    private final Drive googleDriveClient;

    public boolean isNewerVersionAvailable(String fileId, String localPath) {
        try {
            File remoteFile =
                    googleDriveClient.files().get(fileId).setFields("modifiedTime").execute();

            long remoteTime = remoteFile.getModifiedTime().getValue();
            long localTime = Files.getLastModifiedTime(Paths.get(localPath)).toMillis();

            // 수정시간이 로컬보다 크면 업데이트
            return remoteTime > localTime;
        } catch (Exception e) {
            log.warn("file modification comparison failed - new FIle: {}", e.getMessage());
            return true;
        }
    }

    public void downloadDatabaseFile(String fileId, String localPath) {
        // log.info("Downloading DB for user: " + userId + " to" + localPath);
        try (OutputStream outputStream = new FileOutputStream(localPath)) {
            googleDriveClient.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            log.info("file in google drive download success: {}", localPath);
        } catch (Exception e) {
            log.error("error occured white downloading google drive: {}", e.getMessage());
            throw new BusinessException(ErrorCode.FILE_DOWNLOAD_FAILED);
        }
    }


    public String createFinanceSheet(String accessTokenValue, String userEmail)
            throws IOException, GeneralSecurityException {
        // 토큰으로 credentail 만들기
        AccessToken token = new AccessToken(accessTokenValue, null);
        GoogleCredentials credentials = GoogleCredentials.create(token);

        // 드라이브 서비스
        Drive service = new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                        .setApplicationName("PorTracker").build();

        // 파일 설정
        File fileMetadata = new File();
        fileMetadata.setName("PorTracker_db_" + userEmail);
        fileMetadata.setMimeType("application/vnd.google-apps.spreadsheet");

        // 생성 및 id반환
        File file = service.files().create(fileMetadata).setFields("id").execute();
        return file.getId();
    }



}
