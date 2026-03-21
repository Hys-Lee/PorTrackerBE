package com.PorTracker.PorTrackerBE.global.config;

import com.PorTracker.PorTrackerBE.domain.actual_portfolio.entity.TransactionType;
import com.PorTracker.PorTrackerBE.domain.memo.entity.Evaluation;
import com.PorTracker.PorTrackerBE.domain.memo.entity.Importance;
import com.PorTracker.PorTrackerBE.domain.memo.entity.MemoType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${CLIENT_URL}")
    String ClientUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry){
         registry.addMapping("/api/**")
                .allowedOrigins(ClientUrl) // 프론트 주소
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

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
