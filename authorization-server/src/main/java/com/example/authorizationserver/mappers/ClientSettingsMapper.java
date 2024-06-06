package com.example.authorizationserver.mappers;


import com.example.authorizationserver.entities.ClientSettingsEntity;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

public class ClientSettingsMapper {

    public static ClientSettingsEntity toEntity(ClientSettings clientSettings) {
        return ClientSettingsEntity.builder()
                .isRequiredAuthorizationConsent(clientSettings.isRequireAuthorizationConsent())
                .build();
    }

    public static ClientSettings toModel(ClientSettingsEntity clientSettingsEntity) {
        return ClientSettings.builder()
                .requireAuthorizationConsent(clientSettingsEntity.isRequiredAuthorizationConsent())
                .build();
    }
}
