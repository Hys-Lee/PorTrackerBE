package com.PorTracker.PorTrackerBE.service;

import com.PorTracker.PorTrackerBE.constant.SheetSchema;
import com.PorTracker.PorTrackerBE.dto.TransactionDto;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoogleSheetService {
    // 구글 시트에서 데이터 읽어와 dto리스트로 변환
    public List<TransactionDto> getTransactions(String accessTokenValue, String spreadsheetId,
            String range) throws Exception {
        AccessToken token = new AccessToken(accessTokenValue, null);
        GoogleCredentials credentials = GoogleCredentials.create((token));

        Sheets service = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                        .setApplicationName("PorTracker").build();

        ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();

        List<List<Object>> values = response.getValues();

        // Validation - 헤더
        Map<SheetSchema, Integer> colMap = validateAndMapHeaders(values.get((0)));

        List<TransactionDto> result = new ArrayList<>();

        // 데이터 없거나 헤더-컬럼 만 있다면 빈 리스트 반환
        if (values == null || values.size() <= 1) {
            // return result;
            return List.of();
        }

        // 데이터 변환 (Row->DTO), 1부터 시작해 헤더 패스하기
        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);

            // String date = row.size() > 0 ? row.get(0).toString() : "";
            // String category = row.size() > 1 ? row.get(1).toString() : "";
            // String item = row.size() > 2 ? row.get(2).toString() : "";

            // // 숫자 변환 시, 문자 섞일 수 있으므로 숫자만 regexp로 추출
            // long amount = 0;
            // if (row.size() > 3) {
            // String amountStr = row.get(3).toString().replaceAll("[^0-9]", "");
            // if (!amountStr.isEmpty()) {
            // amount = Long.parseLong(amountStr);
            // }
            // }

            // String memo = row.size() > 4 ? row.get(4).toString() : "";

            // result.add(new TransactionDto(date, category, item, amount, memo));
            result.add(parseRow(row, colMap));
        }
        return result;
    }

    private Map<SheetSchema, Integer> validateAndMapHeaders(List<Object> headerRow) {
        Map<SheetSchema, Integer> map = new HashMap<>();

        for (SheetSchema schema : SheetSchema.values()) {
            int index = headerRow.indexOf(schema.getHeaderName());

            // 필수 헤더 못찾으면 에러
            if (schema.isRequired() && index == -1) {
                throw new RuntimeException("필수 헤더가 없습니다: " + schema.getHeaderName());
            }
            map.put(schema, index);
        }
        return map;
    }

    private TransactionDto parseRow(List<Object> row, Map<SheetSchema, Integer> colMap) {
        return new TransactionDto(getSafeValue(row, colMap.get(SheetSchema.DATE)),
                getSafeValue(row, colMap.get(SheetSchema.CATEGORY)),
                getSafeValue(row, colMap.get(SheetSchema.ITEM)),
                parseAmount(getSafeValue(row, colMap.get(SheetSchema.AMOUNT))),
                getSafeValue(row, colMap.get(SheetSchema.MEMO)));
    }

    private long parseAmount(String val) {
        if (val == null || val.isEmpty())
            return 0L;

        String sanitized = val.replace("[^0-9]", "");
        return sanitized.isEmpty() ? 0L : Long.parseLong(sanitized);
    }

    private String getSafeValue(List<Object> row, Integer index) {
        if (index == null || index < 0 || index >= row.size()) {
            return "";
        }

        Object value = row.get(index);
        return value == null ? "" : value.toString().trim();
    }

    public void setupHeaders(String accessTokenValue, String spreadsheetId)
            throws IOException, GeneralSecurityException {
        AccessToken token = new AccessToken(accessTokenValue, null);
        GoogleCredentials credentials = GoogleCredentials.create(token);

        Sheets service = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                        .setApplicationName(("PorTracker")).build();

        // 첫 줄 헤더 작성 - 스키마 참고하기
        List<Object> headerNames = Arrays.stream(SheetSchema.values())
                .map(SheetSchema::getHeaderName).collect(Collectors.toList());
        List<List<Object>> values = Collections.singletonList(headerNames);

        ValueRange body = new ValueRange().setValues(values);

        service.spreadsheets().values().update(spreadsheetId, "A1", body).setValueInputOption("RAW")
                .execute();
    }

    // 구조 변경 이후

    // 구글 드라이브의 파일 수정시간 체크
    public boolean isNewerVersionAvailable(String userId, String localFilePath) {
        try {
            // 임시
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // 구글 드라이브에서 db파일 다운로드
    public void downloadDatabaseFile(String fileId, String localPath) {
        try {
            // 임시
            log.info("file download from google drive succeed!: " + localPath);

        } catch (Exception e) {
            throw new RuntimeException("file download failed", e);
        }
    }
}
