package com.app.ecommerce.category.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@Slf4j
public class ResourceServerSecurityConfig {

    @Value("${jwksUri}")
    private String jwksUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.oauth2ResourceServer(
                r -> {
                    r.jwt(
                            jwtConfigurer -> jwtConfigurer.jwkSetUri(jwksUri)
                                    .jwtAuthenticationConverter(jwtAuthenticationConverter()) // for customizing token to hold the authorities
                    );

                }
        );

        http.authorizeHttpRequests(
                authorizeHttpRequestConfigurer -> authorizeHttpRequestConfigurer
                        .requestMatchers(HttpMethod.GET,"/api/v1/categories")
                            .hasAuthority("READ")
                        .requestMatchers(HttpMethod.POST, "/api/v1/categories")
                            .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/{id}")
                            .hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/{id}")
                            .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/categories/{id}")
                            .hasRole("ADMIN")
                        .anyRequest().authenticated()

        );


        return http.build();
    }



    @Bean
    public Converter<Jwt, JwtAuthenticationToken> jwtAuthenticationConverter() {
        return new JwtAuthenticationConverter();
    }
}

