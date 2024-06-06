package com.example.authorizationserver.mappers;

import com.example.authorizationserver.entities.RedirectUriEntity;
import com.example.authorizationserver.entities.ScopeEntity;

import java.util.Set;
import java.util.stream.Collectors;

public class ScopeMapper {
    public static ScopeEntity toEntity(String scope) {
        return ScopeEntity.builder()
                .value(scope)
                .build();
    }

    public static Set<ScopeEntity> toEntities(Set<String> scopeSet) {
        return scopeSet.stream()
                .map(ScopeMapper::toEntity)
                .collect(Collectors.toSet());
    }

    public static String toModel(ScopeEntity scopeEntity) {
        return scopeEntity.getValue();
    }


    public static Set<String> toModels(Set<ScopeEntity> scopeEntitySet) {
        return scopeEntitySet.stream()
                .map(ScopeMapper::toModel)
                .collect(Collectors.toSet());
    }
}
