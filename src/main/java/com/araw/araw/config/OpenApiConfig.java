package com.araw.araw.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI arawOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ARAW API")
                        .description("API documentation for the ARAW backend services.")
                        .version("v1.0.0")
                        .contact(new Contact().name("ARAW Team").email("support@araw.org"))
                        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("ARAW Project")
                        .url("https://github.com/araw-org"));
    }
}
