package com.example.authorizationserver.mappers;

import com.example.authorizationserver.entities.GrantTypeEntity;
import com.example.authorizationserver.entities.RedirectUriEntity;
import com.example.authorizationserver.entities.TokenSettingsEntity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;

public class TokenSettingsMapper {
    public static TokenSettingsEntity toEntity(TokenSettings tokenSettings) {
        return TokenSettingsEntity.builder()
                .accessTokenTTL(tokenSettings.getAccessTokenTimeToLive())
                .type(tokenSettings.getAccessTokenFormat().getValue())
                .build();
    }

    public static TokenSettings toModel(TokenSettingsEntity tokenSettingsEntity) {
        return TokenSettings.builder()
                .accessTokenTimeToLive(tokenSettingsEntity.getAccessTokenTTL())
                .accessTokenFormat( new OAuth2TokenFormat(tokenSettingsEntity.getType()))
                .refreshTokenTimeToLive(Duration.ofDays(300))
                .build();
    }

}
