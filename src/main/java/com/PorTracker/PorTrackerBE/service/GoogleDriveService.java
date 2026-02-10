package com.PorTracker.PorTrackerBE.service;

import com.PorTracker.PorTrackerBE.global.constant.FileConstants;
import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleDriveService {

    // private final Drive googleDriveClient;
    // private final SqliteDatabaseManager dbManager;

    private final NetHttpTransport transport;
    private final JsonFactory jsonFactory;

    private Drive getService(String token) {
        return new Drive.Builder(transport, jsonFactory,
                r -> r.getHeaders().setAuthorization("Bearer " + token))
                        .setApplicationName("PorTracker").build();
    }

    public String findFileIdByName(String fileName, String token) {
        try {
            // FileList result = googleDriveClient.files().list()
            // .setQ("name = '" + fileName + "' and trashed = false").execute();
            FileList result = getService(token).files().list()
                    .setQ("name = '" + fileName + "' and trashed = false").execute();
            return result.getFiles().isEmpty() ? null : result.getFiles().get(0).getId();

        } catch (Exception e) {
            return null;
        }
    }

    // public String uploadFile(String fileName, String localPath) {
    public String uploadFile(String fileName, String localPath, String token) {
        try {
            java.io.File localFile = new java.io.File(localPath);
            File metadata =
                    new File().setName(fileName).setMimeType(FileConstants.SQLITE_MIME_TYPE);
            FileContent content = new FileContent(FileConstants.SQLITE_MIME_TYPE, localFile);

            // File uploadedFile =
            // googleDriveClient.files().create(metadata, content).setFields("id").execute();
            File uploadedFile =
                    getService(token).files().create(metadata, content).setFields("id").execute();
            return uploadedFile.getId();
        } catch (Exception e) {
            log.error("google drive error: ", e);
            throw new BusinessException(ErrorCode.FILE_DOWNLOAD_FAILED);
        }
    }

    public boolean isNewerVersionAvailable(String fileId, String localPath, String token) {
        try {
            File remoteFile =
                    // googleDriveClient.files().get(fileId).setFields("modifiedTime").execute();
                    getService(token).files().get(fileId).setFields("modifiedTime").execute();

            long remoteTime = remoteFile.getModifiedTime().getValue();
            long localTime = Files.getLastModifiedTime(Paths.get(localPath)).toMillis();

            // 수정시간이 로컬보다 크면 업데이트
            return remoteTime > localTime;
        } catch (Exception e) {
            log.warn("file modification comparison failed - new FIle: {}", e.getMessage());
            return true;
        }
    }

    public void downloadDatabaseFile(String fileId, String localPath, String token) {
        // log.info("Downloading DB for user: " + userId + " to" + localPath);
        try (OutputStream outputStream = new FileOutputStream(localPath)) {
            // googleDriveClient.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            getService(token).files().get(fileId).executeMediaAndDownloadTo(outputStream);
            log.info("file in google drive download success: {}", localPath);
        } catch (Exception e) {
            log.error("error occured white downloading google drive: {}", e.getMessage());
            throw new BusinessException(ErrorCode.FILE_DOWNLOAD_FAILED);
        }
    }

    /// deprecated??
    // public String getOrCreateFileId(String userId) {
    // try {
    // // 파일 존재 확인

    // String fileName = userId + FileConstants.DB_FILE_SUFFIX;
    // FileList result = googleDriveClient.files().list()
    // .setQ("name = '" + fileName + "' and trashed = false").execute();

    // if (!result.getFiles().isEmpty()) {
    // log.info(("found db file"));
    // return result.getFiles().get(0).getId();
    // }

    // // 빈 파일 생성 후 업로드
    // log.info("start creating empty db file: {}", fileName);

    // String tempPath = FileConstants.DB_STORAGE_ROOT + userId + "_init.db";
    // dbManager.createInitialFile(tempPath);

    // java.io.File localFile = new java.io.File(tempPath);
    // File fileMetaData = new File();
    // fileMetaData.setName(fileName);
    // fileMetaData.setMimeType("application/x-sqlite3");

    // FileContent mediaContent = new FileContent(FileConstants.SQLITE_MIME_TYPE, localFile);

    // File uploadedFile = googleDriveClient.files().create(fileMetaData, mediaContent)
    // .setFields("id").execute();

    // localFile.delete();
    // return uploadedFile.getId();
    // } catch (Exception e) {
    // log.error("failed to create file in google drive!", e);
    // throw new BusinessException(ErrorCode.FILE_DOWNLOAD_FAILED);
    // }
    // }

    //////////////////////////
    /** deprecated */
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
