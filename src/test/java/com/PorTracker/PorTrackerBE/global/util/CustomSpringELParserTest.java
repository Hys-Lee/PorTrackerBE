package com.PorTracker.PorTrackerBE.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CustomSpringELParserTest {

    @Test
    @DisplayName("파라미터 바인딩을 포함한 SpEL 표현식 동적 해석 검증")
    void parseSpelExpressionSuccess() {
        // given
        String[] parameterNames = {"userId", "actionName"};
        Object[] args = {"user-1234", "backup"};
        String keyExpression = "'lock:' + #userId + ':' + #actionName";

        // when
        Object parsedValue = CustomSpringELParser.getDynamicValue(parameterNames, args, keyExpression);

        // then
        assertThat(parsedValue).isEqualTo("lock:user-1234:backup");
    }

    @Test
    @DisplayName("잘못된 SpEL 표현식 입력 시 원본 문자열 그대로 반환하는 예외 처리 검증")
    void parseInvalidSpelExpressionFallback() {
        // given
        String[] parameterNames = {"userId"};
        Object[] args = {"user-1234"};
        String invalidExpression = "invalid#expression%"; // 파싱 불가능한 구문

        // when
        Object parsedValue = CustomSpringELParser.getDynamicValue(parameterNames, args, invalidExpression);

        // then
        assertThat(parsedValue).isEqualTo(invalidExpression);
    }
}
