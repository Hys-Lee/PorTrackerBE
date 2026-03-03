package com.PorTracker.PorTrackerBE.global.infra.google;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleDriveClient {
    private final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
    private final RestTemplate restTemplate = new RestTemplate(requestFactory);

    private static final String DRIVE_API_URL = "https://www.googleapis.com/upload/drive/v3/files";

    private static final String UPLOAD_API_URL = "https://www.googleapis.com/upload/drive/v3/files";

    public void createNewFile(String userId, String accessToken){
        // Path filePath = Paths.get("db/"+userId+".db");
        // if(!Files.exists(filePath)) return;

        // // 메타데이터
        // String metadata = "{ \"name\": \"" + userId+".db\" }";

        // HttpHeaders headers = new HttpHeaders();
        // headers.setBearerAuth(accessToken);
        // headers.setContentType(MediaType.parseMediaType("multipart/realted;boundary=foo"));

        // String body = "--foo\r\n"+"Content-Type: application/json; charset=UTF-8\r\n\r\n"+metadata+"\r\n"+"--foo\r\n"+"Content-Type: application/x-sqlite3\r\n\r\n";

        // try{
        //     byte[] fileContent = Files.readAllBytes(filePath);
        //     byte[] endBoundary = "\r\n--foo--".getBytes();

        //     // 조립
        //     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //     outputStream.write(body.getBytes());
        //     outputStream.write(fileContent);
        //     outputStream.write(endBoundary);

        //     HttpEntity<byte[]> entity = new HttpEntity<>(outputStream.toByteArray(),headers);

        //     // google drive api
        //     restTemplate.postForEntity(DRIVE_API_URL+"?uploadType=multipart", entity, String.class);

        //     log.info("[GoogleDrive] database uploaded successfully for user: {}",userId);

        // }catch (Exception e){
        //     log.error("[Googledrive] Upload failed for user: {}",userId,e);
        // }

        try{

            Path filePath = Paths.get("db/"+userId+".db");
            if(!Files.exists(filePath)) return;
            
            HttpHeaders mainHeaders = new HttpHeaders();
            mainHeaders.setBearerAuth(accessToken);
            mainHeaders.setContentType(MediaType.parseMediaType("multipart/related;boundary=db_sync_boundary"));
            
            HttpHeaders metadataHeaders = new HttpHeaders();
            metadataHeaders.setContentType(MediaType.APPLICATION_JSON);
            String metadata = "{ \"name\": \""+userId+".db\" }";
            HttpEntity<String> metadataPart = new HttpEntity<>(metadata,metadataHeaders);
            
            HttpHeaders binaryDataHeaders = new HttpHeaders();
            binaryDataHeaders.setContentType(MediaType.parseMediaType("application/x-sqlite3"));
            byte[] fileBytes = Files.readAllBytes(filePath);
            HttpEntity<byte[]> filepart = new HttpEntity<>(fileBytes,binaryDataHeaders);
            
            // 조립
            byte[] requestBody = createMultipartBody("db_sync_boundary", metadataPart, filepart);
            
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(requestBody, mainHeaders);
            ResponseEntity<String> response = restTemplate.postForEntity(DRIVE_API_URL+"?uploadType=multipart", requestEntity, String.class);

            log.info("[GoogleDrive] api response:{}, api body:{}", response.getStatusCode(), response.getBody());

            log.info("[GoogleDrive] db uploaed successfully for user: {}",userId);

        }catch(Exception e){
            log.error("[GoogleDrive] Upload failed for user: {}");
        }


    }

    private byte[] createMultipartBody(String boundary, HttpEntity<String> metadataPart, HttpEntity<byte[]>filePart) throws IOException{
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        // 메타 데이터 파트
        os.write(("--"+boundary+"\r\n").getBytes());
        os.write(("Content-Type: application/json; charset=UTF-8\r\n\r\n").getBytes());
        os.write(metadataPart.getBody().getBytes(StandardCharsets.UTF_8));
        os.write("\r\n".getBytes());

        // 파일 파트
        os.write(("--"+boundary+"\r\n").getBytes());
        os.write(("Content-Type: "+filePart.getHeaders().getContentType()+"\r\n\r\n").getBytes());
        os.write(filePart.getBody());

        // 전체 종료
        os.write(("\r\n--"+boundary+"--").getBytes());
        return os.toByteArray();
    }


    public String findFileIdByName(String fileName, String accessToken){
         String url = "https://www.googleapis.com/drive/v3/files?q=name='" + fileName + "' and trashed=false";

         HttpHeaders headers = new HttpHeaders();
         headers.setBearerAuth(accessToken);

        //  try{
        ResponseEntity<Map> response  = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        List<Map<String, Object>> files = (List<Map<String,Object>>)response.getBody().get("files");
        if(files != null && !files.isEmpty()){
            return (String) files.get(0).get("id");
        }
        //  }catch(Exception e){
        //     log.error("[GoogleDrive] Search failed for file: {}", fileName, e);
        //  }
         return null;
    }

    public void syncToDrive(String userId, String accessToken){
        String fileName = userId+".db";
        String fileId = findFileIdByName(fileName, accessToken);
        if(fileId != null){
            updateExistingFile(fileId, userId, accessToken);
        }else{
            // createNewFile(userId, accessToken);
            createNewFile(userId, accessToken);    
        }
    }

    private void updateExistingFile(String fileId, String userId, String accessToken){
        String url = UPLOAD_API_URL+"/"+fileId+"?uploadType=media";
        Path filePath = Paths.get("db/"+userId+".db");

        try{
            byte[] fileBytes = Files.readAllBytes(filePath);
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.parseMediaType("application/x-sqlite3"));

            HttpEntity<byte[]> entity = new HttpEntity<>(fileBytes, headers);
            restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);
            log.info("[GoogleDrive] Updated existing file: {}", fileId);
        }catch (Exception e){
            log.error("[GoogleDrive] Update failed: {}", e.getMessage());
        }
    }

    // private void createNewFile(String userId, String accessToken)[

    // ]

    
    public byte[] downloadFile(String fileId, String accessToken){
        String url = DRIVE_API_URL+"/"+fileId+"?alt=media";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        try{
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
            return response.getBody();
        }catch(Exception e){
            log.error("[GoogleDrive] Download failed: {}", e.getMessage());
            return null;
        }
    }


    public void deleteFile(String fileName, String accessToken){
        String fileId = findFileIdByName(fileName, accessToken);
        if (fileId == null) return;

        String url = "https://www.googleapis.com/drive/v3/files/"+fileId;
        HttpHeaders headers = new HttpHeaders();

        try{
            restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
            log.info("[GoogleDrive] Deleted file: {}", fileName);
        }catch (Exception e){
            log.error("[GoogleDrive] Failed to delete file: {}", fileName, e.getMessage());
        }
    }

}
