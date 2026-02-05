package com.PorTracker.PorTrackerBE.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FinanceCompareRequest(

        @NotBlank(message = "구글 엑세스 토큰은 필수입니다.") String accessToken,

        @NotBlank(message = "스프레드시트 ID는 필수입니다.") String spreadsheetId,

        @NotNull(message = "나이 정보는 필수입니다.") @Min(value = 1,
                message = "나이는 1세 이상이어야 합니다.") Integer age,

        @NotBlank(message = "직업 정보는 필수입니다.") String job

) {

}
