package com.example.authorizationserver.mappers;


import com.example.authorizationserver.entities.ClientEntity;
import com.example.authorizationserver.entities.RedirectUriEntity;

import java.util.Set;
import java.util.stream.Collectors;


public class RedirectUriMapper {

    public static RedirectUriEntity toEntity(String redirectUri , ClientEntity clientEntity) {
        return RedirectUriEntity.builder()
                .value(redirectUri)
                .client(clientEntity)
                .build();
    }


    public static Set<RedirectUriEntity> toEntities(Set<String> redirectUriSet , ClientEntity clientEntity) {
        return redirectUriSet
                .stream()
                .map(model -> RedirectUriMapper.toEntity(model , clientEntity))
                .collect(Collectors.toSet());
    }


    public static String toModel(RedirectUriEntity redirectUriEntity) {
        return redirectUriEntity.getValue();
    }



    public static Set<String> toModels(Set<RedirectUriEntity> redirectUriEntitySet) {
        return redirectUriEntitySet.stream()
                .map(RedirectUriMapper::toModel)
                .collect(Collectors.toSet());
    }
}
