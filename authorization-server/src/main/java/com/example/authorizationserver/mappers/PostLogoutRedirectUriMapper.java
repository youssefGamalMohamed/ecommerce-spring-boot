package com.example.authorizationserver.mappers;

import com.example.authorizationserver.entities.ClientEntity;
import com.example.authorizationserver.entities.PostLogoutUriEntity;
import com.example.authorizationserver.entities.RedirectUriEntity;

import java.util.Set;
import java.util.stream.Collectors;

public class PostLogoutRedirectUriMapper {

    public static PostLogoutUriEntity toEntity(String postLogoutUri , ClientEntity clientEntity) {
        return PostLogoutUriEntity.builder()
                .value(postLogoutUri)
                .client(clientEntity)
                .build();
    }


    public static Set<PostLogoutUriEntity> toEntities(Set<String> postRedirectUris , ClientEntity clientEntity) {
        return postRedirectUris
                .stream()
                .map(model -> PostLogoutRedirectUriMapper.toEntity(model , clientEntity))
                .collect(Collectors.toSet());
    }


    public static String toModel(PostLogoutUriEntity postLogoutUriEntity) {
        return postLogoutUriEntity.getValue();
    }



    public static Set<String> toModels(Set<PostLogoutUriEntity> postLogoutUriEntitySet) {
        return postLogoutUriEntitySet.stream()
                .map(PostLogoutRedirectUriMapper::toModel)
                .collect(Collectors.toSet());
    }

}
