package com.PorTracker.PorTrackerBE.domain.currency.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyTypeRequest {
    @NotBlank(message = "통화 코드가 없습니다.")
    @Pattern(regexp = "^[a-zA-Z]{3}$", message = "영문 3글자가 아닌 통화코드입니다.")
    private String code;
}
