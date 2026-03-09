package com.PorTracker.PorTrackerBE.global.config;

import com.PorTracker.PorTrackerBE.global.error.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {
    static {
        // Enum을 스캔할 때 실제 값(JsonValue 등)을 기준으로 리스트를 만들도록 설정
        io.swagger.v3.core.jackson.ModelResolver.enumsAsRef = true;
    }

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "jwtAuth";

        // api 요청 시 인증 필요 항목 설정
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        // ResolvedSchema resolvedSchema =
        //         ModelConverters.getInstance().readAllAsResolvedSchema(ErrorResponse.class);

        Components components =
                new Components()
                        // .addSchemas("ErrorResponse", resolvedSchema.schema)
                        .addSecuritySchemes(
                                jwtSchemeName,
                                new SecurityScheme()
                                        .name(jwtSchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"));

        return new OpenAPI()
                .info(
                        new Info()
                                .title("PorTracker API Document")
                                .description("PorTracker의 백엔드 API 문서")
                                .version("v1.0.0"))
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    @Bean
    public OpenApiCustomizer customerGlobalHeaderOpenApiCustomiser() {
        return openApi -> {
            Map<String, Schema> schemas = ModelConverters.getInstance().read(ErrorResponse.class);
            schemas.forEach((key, schema) -> openApi.getComponents().addSchemas(key, schema));
            openApi.getPaths()
                    .values()
                    .forEach(
                            pathItem ->
                                    pathItem.readOperations()
                                            .forEach(
                                                    operation -> {
                                                        ApiResponses responses =
                                                                operation.getResponses();

                                                        // 공통 에러 응답 정의
                                                        ApiResponse errorResponse =
                                                                new ApiResponse()
                                                                        .description(
                                                                                "에러 발생 (공통 구조)")
                                                                        .content(
                                                                                new Content()
                                                                                        .addMediaType(
                                                                                                "application/json",
                                                                                                new MediaType()
                                                                                                        .schema(
                                                                                                                new Schema<>()
                                                                                                                        .$ref(
                                                                                                                                "#/components/schemas/ErrorResponse"))));

                                                        // 모든 api에 공통 에러 응답 추가
                                                        if (!responses.containsKey("400"))
                                                            responses.addApiResponse(
                                                                    "400", errorResponse);
                                                        if (!responses.containsKey("401"))
                                                            responses.addApiResponse(
                                                                    "401", errorResponse);
                                                        if (!responses.containsKey("500"))
                                                            responses.addApiResponse(
                                                                    "500", errorResponse);
                                                    }));
        };
    }
}
