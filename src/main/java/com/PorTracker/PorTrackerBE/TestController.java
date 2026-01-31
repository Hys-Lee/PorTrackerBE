package com.PorTracker.PorTrackerBE;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// @RestController
// public class TestController {
//     @GetMapping("/hello")
//     public String hello() {
//         return "SpringBoot is running";
//     }

// }

@RestController
@RequiredArgsConstructor // 상단에 추가 (Lombok)
public class TestController {
    private final GoogleSheetService googleSheetService;

    @GetMapping("/data")
    public List<List<Object>> getData() throws Exception {
        // 시트 URL에서 ID 추출: https://docs.google.com/spreadsheets/d/[이부분이ID]/edit
        String spreadsheetId = "1CvCmwDyw8NxBhBpOY2jXVePad01xICOk6WpZVPmRR_Y";
        String range = "시트1!A1:E10"; // 가져올 범위
        return googleSheetService.getSheetData(spreadsheetId, range);
    }
}
