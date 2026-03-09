package com.PorTracker.PorTrackerBE.domain.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TagCreateRequest {
    @Schema(description = "태그 내용", example = "중요")
    @NotBlank
    @Size(max = 15)
    private String content;

    public TagCreateRequest(String content) {
        this.content = content;
    }
}
