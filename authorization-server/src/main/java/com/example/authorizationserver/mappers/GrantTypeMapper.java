package com.example.authorizationserver.mappers;


import com.example.authorizationserver.entities.GrantTypeEntity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.Set;
import java.util.stream.Collectors;

public class GrantTypeMapper {

    public static GrantTypeEntity toEntity(AuthorizationGrantType authorizationGrantType) {
        return GrantTypeEntity.builder()
                .value(authorizationGrantType.getValue())
                .build();
    }

    public static Set<GrantTypeEntity> toEntities(Set<AuthorizationGrantType> authorizationGrantTypeSet) {
        return authorizationGrantTypeSet
                .stream()
                .map(GrantTypeMapper::toEntity)
                .collect(Collectors.toSet());
    }

    public static AuthorizationGrantType toModel(GrantTypeEntity grantTypeEntity) {
        return new AuthorizationGrantType(grantTypeEntity.getValue());
    }

    public static Set<AuthorizationGrantType> toModels(Set<GrantTypeEntity> grantTypeEntitySet) {
        return grantTypeEntitySet.stream()
                .map(GrantTypeMapper::toModel)
                .collect(Collectors.toSet());
    }
}
