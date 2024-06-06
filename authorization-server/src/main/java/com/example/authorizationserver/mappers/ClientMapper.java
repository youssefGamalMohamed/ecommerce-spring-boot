package com.example.authorizationserver.mappers;


import com.example.authorizationserver.entities.ClientEntity;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.HashSet;


public class ClientMapper {

    public static ClientEntity toEntity(RegisteredClient registeredClient) {
        ClientEntity clientEntity =  ClientEntity.builder()
                .clientId(registeredClient.getClientId())
                .clientSecret(registeredClient.getClientSecret())
                .clientName(registeredClient.getClientName())
                .authenticationMethods(
                        AuthenticationMethodMapper.toEntities(registeredClient.getClientAuthenticationMethods())

                )
                .grantTypes(
                        GrantTypeMapper.toEntities(registeredClient.getAuthorizationGrantTypes())
                )
                .redirectUris(new HashSet<>())
                .scopes(
                        ScopeMapper.toEntities(registeredClient.getScopes())
                )
                .tokenSettings(
                        TokenSettingsMapper.toEntity(registeredClient.getTokenSettings())
                )
                .clientSettings(
                        ClientSettingsMapper.toEntity(registeredClient.getClientSettings())
                )
                .postLogoutUris(
                        new HashSet<>()
                )
                .build();

        clientEntity.getRedirectUris().addAll(
                RedirectUriMapper.toEntities(registeredClient.getRedirectUris() , clientEntity)
        );

        clientEntity.getPostLogoutUris().addAll(
            PostLogoutRedirectUriMapper.toEntities(registeredClient.getPostLogoutRedirectUris() , clientEntity)
        );

        return clientEntity;
    }

    public static RegisteredClient toModel(ClientEntity clientEntity) {
        return RegisteredClient
                .withId(String.valueOf(clientEntity.getId()))
                .clientId(clientEntity.getClientId())
                .clientSecret(clientEntity.getClientSecret())
                .clientName(clientEntity.getClientName())
                .clientAuthenticationMethods(
                        authenticationMethods -> authenticationMethods.addAll(AuthenticationMethodMapper.toModels(clientEntity.getAuthenticationMethods()))
                )
                .authorizationGrantTypes(
                        grantTypes -> grantTypes.addAll(GrantTypeMapper.toModels(clientEntity.getGrantTypes()))
                )
                .redirectUris(
                        redirectUrisSet -> redirectUrisSet.addAll(RedirectUriMapper.toModels(clientEntity.getRedirectUris()))
                )
                .scopes(
                        scopes -> scopes.addAll(ScopeMapper.toModels(clientEntity.getScopes()))
                )
                .tokenSettings(TokenSettingsMapper.toModel(clientEntity.getTokenSettings()))
                .clientSettings(ClientSettingsMapper.toModel(clientEntity.getClientSettings()))
                .postLogoutRedirectUris(
                        postLogoutUris -> postLogoutUris.addAll(PostLogoutRedirectUriMapper.toModels(clientEntity.getPostLogoutUris()))
                )
                .build();
    }
}
