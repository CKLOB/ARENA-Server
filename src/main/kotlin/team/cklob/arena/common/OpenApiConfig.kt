package team.cklob.arena.common

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI =
        OpenAPI().components(
            Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"),
                ).addSchemas(
                    "CommonErrorResponse",
                    ObjectSchema()
                        .addProperty("code", StringSchema().example(ErrorCode.INVALID_REQUEST.name))
                        .addProperty("message", StringSchema().example(ErrorCode.INVALID_REQUEST.message))
                        .addProperty("data", ObjectSchema().nullable(true)),
                ),
        )
}
