package com.app.ecommerce.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
            title = "Ecommerce App Swagger",
            version = "1.0",
            description = "this API represent the online selling of products for customers all over the world"
        )
)
@Configuration
public class OpenApiDocumentationConfiguration {
}
