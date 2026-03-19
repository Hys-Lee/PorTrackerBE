package com.PorTracker.PorTrackerBE.global.config;

import com.PorTracker.PorTrackerBE.domain.actual_portfolio.entity.TransactionType;
import com.PorTracker.PorTrackerBE.domain.memo.entity.Evaluation;
import com.PorTracker.PorTrackerBE.domain.memo.entity.Importance;
import com.PorTracker.PorTrackerBE.domain.memo.entity.MemoType;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // registry.addConverterFactory(new GenericEnumConverterFactory());

        // MemoType
        registry.addConverter(String.class, MemoType.class, MemoType::from);

        // Importance
        registry.addConverter(String.class, Importance.class, Importance::from);

        // Evaluation
        registry.addConverter(String.class, Evaluation.class, Evaluation::from);

        // TransactionType
        registry.addConverter(String.class, TransactionType.class, TransactionType::from);
    }
}
