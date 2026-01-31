package com.PorTracker.PorTrackerBE;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GoogleSheetService {

    private static final String APPLICATION_NAME = "My Finance App";
    private static final String JSON_FACTORY = "GSON";

    public List<List<Object>> getSheetData(String spreadsheetId, String range)
            throws IOException, GeneralSecurityException {
        // 1. 인증 정보 로드 (아까 받은 JSON 파일)
        GoogleCredentials credentials =
                GoogleCredentials.fromStream(
                                new FileInputStream("src/main/resources/google-key.json"))
                        .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));

        // 2. 구글 시트 서비스 객체 생성
        Sheets service =
                new Sheets.Builder(
                                GoogleNetHttpTransport.newTrustedTransport(),
                                GsonFactory.getDefaultInstance(),
                                new HttpCredentialsAdapter(credentials))
                        .setApplicationName(APPLICATION_NAME)
                        .build();

        // 3. 데이터 가져오기
        ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();

        return response.getValues();
    }
}
