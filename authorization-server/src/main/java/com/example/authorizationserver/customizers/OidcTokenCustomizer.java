package com.example.authorizationserver.customizers;

import com.example.authorizationserver.entities.UserEntity;
import com.example.authorizationserver.wrappers.SecurityUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.Objects;

@Component
@AllArgsConstructor
@Slf4j
public class OidcTokenCustomizer {

    private final ObjectMapper objectMapper;
    private final ClaimsExtractor claimsExtractor;

    public void customize(JwtEncodingContext context) {
        if (context.getPrincipal() instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) context.getPrincipal();
            SecurityUser securityUser = (SecurityUser) authenticationToken.getPrincipal();
            UserEntity userEntity = securityUser.getUserEntity();
            try {
                Map<String, Object> claims = objectMapper.readValue(userEntity.getClaims(), Map.class);
                Map<String, Object> requiredClaims = claimsExtractor.extractClaims(claims, context.getAuthorization().getAuthorizedScopes());
                requiredClaims.forEach(context.getClaims()::claim);
            } catch (JsonProcessingException e) {
                log.error("Error processing JSON claims", e);
                throw new RuntimeException(e);
            }
        } else if (context.getPrincipal() instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) context.getPrincipal();
            DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) oAuth2AuthenticationToken.getPrincipal();
            defaultOAuth2User.getAttributes().entrySet().stream()
                    .filter(Objects::nonNull)
                    .filter(entry -> entry.getKey() != null)
                    .filter(entry -> entry.getValue() != null)
                    .forEach(entry -> context.getClaims().claim(entry.getKey(), entry.getValue()));
        }
    }
}
