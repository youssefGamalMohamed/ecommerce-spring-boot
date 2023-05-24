package com.app.ecommerce.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import lombok.extern.log4j.Log4j2;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

@OpenAPIDefinition(
        info = @Info(
            title = "Ecommerce App Swagger",
            version = "1.0",
            description = "this API represent the online selling of products for customers all over the world"
        )
)
@Configuration
@Log4j2
public class OpenApiDocumentationConfiguration {
        @Bean
        public OperationCustomizer customGlobalHeaders() {

                return (Operation operation, HandlerMethod handlerMethod) -> {

                        Parameter authorizationTokenHeader = new Parameter()
                                .in(ParameterIn.HEADER.toString())
                                .schema(new StringSchema())
                                .name("Authorization")
                                .description("Authorization Header Token for Token Based Authentication")
                                .required(true);

                        // remove Authorization header from swagger documentation depend on the name of the "controller function"
                        // that have @PostMapping("/login") and @PostMapping("/login") which name are "login(..)" and "register(..)"
                        if(!(operation.getOperationId().contains("register") || operation.getOperationId().contains("login"))) {
                                operation.addParametersItem(authorizationTokenHeader);
                        }


                        return operation;
                };
        }
}
