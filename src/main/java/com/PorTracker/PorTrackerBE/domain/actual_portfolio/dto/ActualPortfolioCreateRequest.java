package com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto;

import org.springframework.format.annotation.DateTimeFormat;

import com.PorTracker.PorTrackerBE.domain.actual_portfolio.entity.TransactionType;
import com.PorTracker.PorTrackerBE.global.constant.ValidationConstants;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActualPortfolioCreateRequest {

    @JsonProperty("assetId")
    @NotBlank(message = "자산이 지정되지 않았습니다.")
    // private Long assetId;
    @Pattern(regexp = ValidationConstants.UUID_REGEXP,message = "유효한 ID 형식이 아닙니다.")
    private String assetId;

    @JsonProperty("date")
    @NotBlank(message = "날짜가 없습니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) // ISO 허용
    private String date;

    @JsonProperty("transactionType")
    @NotNull(message = "거래 타입을 정하지 않았습니다.")
    private TransactionType transactionType;

    @JsonProperty("currencyId")
    @NotBlank(message = "통화가 지정되지 않았습니다.")
    @Pattern(regexp = ValidationConstants.UUID_REGEXP,message = "유효한 ID 형식이 아닙니다.")
    // private Long currencyId;
    private String currencyId;


    @JsonProperty("priceBp")
    @PositiveOrZero(message = "0이상의 정수가 아닙니다.")
    @NotNull(message = "base point 값이 없습니다.")
    private Long priceBp;

    @JsonProperty("amountBp")
    @PositiveOrZero(message = "0이상의 정수가 아닙니다.")
    @NotNull(message = "base point 값이 없습니다.")
    private Long amountBp;

    @JsonProperty("exchangeRateBp")
    @PositiveOrZero(message = "0이상의 정수가 아닙니다.")
    @NotNull(message = "base point 값이 없습니다.")
    private Long exchangeRateBp;
}
