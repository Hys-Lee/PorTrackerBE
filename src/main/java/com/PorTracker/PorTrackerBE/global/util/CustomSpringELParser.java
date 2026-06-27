package com.PorTracker.PorTrackerBE.global.util;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class CustomSpringELParser {
    private CustomSpringELParser() {}

    /** AOP 메서드의 파라미터와 아규먼트를 기반으로 SpEL 표현식을 해석합니다. */
    public static Object getDynamicValue(String[] parameterNames, Object[] args, String key) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        try {
            return parser.parseExpression(key).getValue(context, Object.class);
        } catch (Exception e) {
            // SpEL 해석 오류 시 락 키를 그대로 반환
            return key;
        }
    }
}
