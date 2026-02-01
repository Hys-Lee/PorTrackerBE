package com.PorTracker.PorTrackerBE.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

@Service
public class GoogleSheetService {
    public void setupHeaders(String accessTokenValue, String spreadsheetId)
            throws IOException, GeneralSecurityException {
        AccessToken token = new AccessToken(accessTokenValue, null);
        GoogleCredentials credentials = GoogleCredentials.create(token);

        Sheets service = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                        .setApplicationName(("PorTracker")).build();

        // 첫 줄 헤더 작성
        List<List<Object>> values =
                Arrays.asList(Arrays.asList("date", "category", "list", "value", "memo"));

        ValueRange body = new ValueRange().setValues(values);

        service.spreadsheets().values().update(spreadsheetId, "A1", body).setValueInputOption("RAW")
                .execute();

    }
}
