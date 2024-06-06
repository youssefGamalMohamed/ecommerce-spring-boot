package com.example.authorizationserver.mappers;


import com.example.authorizationserver.entities.AuthenticationMethodEntity;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.Set;
import java.util.stream.Collectors;

public class AuthenticationMethodMapper {

    public static AuthenticationMethodEntity toEntity(ClientAuthenticationMethod clientAuthenticationMethod) {
        return AuthenticationMethodEntity.builder()
                .value(clientAuthenticationMethod.getValue())
                .build();
    }

    public static Set<AuthenticationMethodEntity> toEntities(Set<ClientAuthenticationMethod> clientAuthenticationMethodSet) {
        return clientAuthenticationMethodSet.stream()
                .map(AuthenticationMethodMapper::toEntity)
                .collect(Collectors.toSet());
    }

    public static ClientAuthenticationMethod toModel(AuthenticationMethodEntity authenticationMethodEntity) {
        return new ClientAuthenticationMethod(authenticationMethodEntity.getValue());
    }


    public static Set<ClientAuthenticationMethod> toModels(Set<AuthenticationMethodEntity> authenticationMethodEntitySet) {
        return authenticationMethodEntitySet.stream()
                .map(AuthenticationMethodMapper::toModel)
                .collect(Collectors.toSet());
    }

}
