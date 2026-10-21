package com.tamdan.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("api-docs")
public class OpenApiConfiguration {

    private static final String JWT_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI tamDanOpenApi() {
        return new OpenAPI()
            .info(new Info().title("Tam Dan API").description("Tam Dan backend API documentation").version("v1"))
            .components(
                new Components()
                    .addSecuritySchemes(
                        JWT_SCHEME,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                    )
            )
            .addSecurityItem(new SecurityRequirement().addList(JWT_SCHEME));
    }
}
