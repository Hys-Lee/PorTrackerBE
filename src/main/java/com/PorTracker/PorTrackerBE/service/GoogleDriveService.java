package com.PorTracker.PorTrackerBE.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.springframework.stereotype.Service;

@Service
public class GoogleDriveService {
    public String createFinanceSheet(String accessTokenValue, String userEmail)
            throws IOException, GeneralSecurityException {
        // 토큰으로 credentail 만들기
        AccessToken token = new AccessToken(accessTokenValue, null);
        GoogleCredentials credentials = GoogleCredentials.create(token);

        // 드라이브 서비스
        Drive service =
                new Drive.Builder(
                                GoogleNetHttpTransport.newTrustedTransport(),
                                GsonFactory.getDefaultInstance(),
                                new HttpCredentialsAdapter(credentials))
                        .setApplicationName("PorTracker")
                        .build();

        // 파일 설정
        File fileMetadata = new File();
        fileMetadata.setName("PorTracker_db_" + userEmail);
        fileMetadata.setMimeType("application/vnd.google-apps.spreadsheet");

        // 생성 및 id반환
        File file = service.files().create(fileMetadata).setFields("id").execute();
        return file.getId();
    }
}
