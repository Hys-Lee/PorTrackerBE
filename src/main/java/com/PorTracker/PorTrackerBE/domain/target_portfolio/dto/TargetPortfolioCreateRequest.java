package com.PorTracker.PorTrackerBE.domain.target_portfolio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TargetPortfolioCreateRequest {
    @NotBlank(message = "이름이 없습니다.")
    @Size(max = 20, message = "이름이 최대 글자수 20자를 초과합니다.")
    @JsonProperty("name")
    private String name;


    @JsonProperty("date")
    @NotBlank(message = "날짜가 없습니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) // ISO 허용
    private String date;

    @JsonProperty("items")
    @NotNull(message = "내용이 없습니다.")
    private List<TargetPortfolioItemRequest> items;
}
