package com.PorTracker.PorTrackerBE.global.common;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * POST / PUT / DELETE 응답에서 해당 엔티티의 public_id를 "id"라는 이름으로 반환하기 위한 공통 DTO. Swagger에서도 정확한 타입이 노출됩니다.
 */
@Schema(description = "변경 작업(Create/Update/Delete) 응답 – 대상 엔티티의 public_id를 id 필드로 반환")
public record IdResponse(
        @Schema(description = "대상 엔티티의 public_id", example = "550e8400-e29b-41d4-a716-446655440000")
                String id) {
    public static IdResponse of(String publicId) {
        return new IdResponse(publicId);
    }
}
