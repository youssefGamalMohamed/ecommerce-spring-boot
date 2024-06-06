package com.example.authorizationserver.customizers;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AccessTokenCustomizer {

    public void customize(JwtEncodingContext context, List<String> additionalAuthorities) {
        var authorities = context.getPrincipal().getAuthorities();
        var authoritiesList = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        authoritiesList.addAll(additionalAuthorities);
        context.getClaims().claim("authorities", authoritiesList);
    }
}
