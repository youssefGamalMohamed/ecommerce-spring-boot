package com.example.authorizationserver.customizers;
import com.example.authorizationserver.entities.UserEntity;
import com.example.authorizationserver.repositories.UserRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class TokenCustomizerImpl implements OAuth2TokenCustomizer<JwtEncodingContext> {

    private final OidcTokenCustomizer oidcTokenCustomizer;
    private final AccessTokenCustomizer accessTokenCustomizer;
    private final UserRepo userRepo;

    @Override
    public void customize(JwtEncodingContext context) {
        log.info("Auth Server Settings = {}", context.getAuthorizationServerContext().getAuthorizationServerSettings());
        log.info("USERNAME = {}", context.getPrincipal().getName());

        if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            Optional<UserEntity> userEntityOptional = userRepo.findByUsername(context.getPrincipal().getName());
            if (userEntityOptional.isEmpty()) {
                accessTokenCustomizer.customize(context, List.of("ROLE_USER", "READ"));
            } else {
                accessTokenCustomizer.customize(context, Collections.emptyList());
            }
        }

        if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
            oidcTokenCustomizer.customize(context);
        }
    }
}
