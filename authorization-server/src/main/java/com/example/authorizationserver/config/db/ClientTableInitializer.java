package com.example.authorizationserver.config.db;


import com.example.authorizationserver.entities.ClientEntity;
import com.example.authorizationserver.mappers.ClientMapper;
import com.example.authorizationserver.repositories.ClientRepo;
import lombok.AllArgsConstructor;
import org.ietf.jgss.Oid;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.Set;

@Configuration
@AllArgsConstructor
public class ClientTableInitializer {

    private final ClientRepo clientRepo;

    public void addAllClients() {
        RegisteredClient ecommerce_front_end_website_client = RegisteredClient.withId("1")
                .clientId("ecommerce-front-end-website-client")
                .clientSecret("password")
                .clientName("ecommerce-front-end-website-client")
                .clientAuthenticationMethod(
                        ClientAuthenticationMethod.CLIENT_SECRET_BASIC
                )
                .authorizationGrantType(
                        AuthorizationGrantType.AUTHORIZATION_CODE
                )
                .authorizationGrantType(
                        AuthorizationGrantType.REFRESH_TOKEN
                )
                .authorizationGrantType(
                        AuthorizationGrantType.CLIENT_CREDENTIALS
                )
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.EMAIL)
                .scope(OidcScopes.PHONE)
                .scope(OidcScopes.ADDRESS)
                .scope(OidcScopes.PROFILE)
                .redirectUris(redirectUris -> redirectUris.add("https://example.com/"))
                .clientSettings(
                        ClientSettings.builder()
                                .requireAuthorizationConsent(true)
                                .build()
                )
                .tokenSettings(
                        TokenSettings.builder()
                                .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED) // for JWT ( Opaque )
                                .accessTokenTimeToLive(Duration.ofHours(5))
                                .build()
                )
                .postLogoutRedirectUris(
                        postLogoutUris -> postLogoutUris.addAll(Set.of("https://springone.io/" , "http://logout-uri-1/2"))
                )
                .build();

        clientRepo.save(ClientMapper.toEntity(ecommerce_front_end_website_client));
    }
}
