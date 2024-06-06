package com.example.authorizationserver.customizers;

import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ClaimsExtractor {

    private static final List<String> PROFILE = List.of(
            "name", "family_name", "given_name", "middle_name", "nickname", "preferred_username", "profile", "picture", "website", "gender", "birthdate", "zoneinfo", "locale"
    );

    private static final List<String> PHONE = List.of("phone_number", "phone_number_verified");

    private static final List<String> EMAIL = List.of("email", "email_verified");

    private static final List<String> ADDRESS = List.of("zoneinfo", "locale");

    public Map<String, Object> extractClaims(Map<String, Object> fullClaims, Set<String> acceptedScopes) {
        Map<String, Object> requiredClaims = new HashMap<>();
        if (acceptedScopes.contains(OidcScopes.PROFILE)) {
            requiredClaims.putAll(extractClaims(PROFILE, fullClaims));
        }
        if (acceptedScopes.contains(OidcScopes.PHONE)) {
            requiredClaims.putAll(extractClaims(PHONE, fullClaims));
        }
        if (acceptedScopes.contains(OidcScopes.EMAIL)) {
            requiredClaims.putAll(extractClaims(EMAIL, fullClaims));
        }
        if (acceptedScopes.contains(OidcScopes.ADDRESS)) {
            requiredClaims.putAll(extractClaims(ADDRESS, fullClaims));
        }
        return requiredClaims;
    }

    private Map<String, Object> extractClaims(List<String> claimsNames, Map<String, Object> fullClaims) {
        Map<String, Object> claims = new HashMap<>();
        for (String claimName : claimsNames) {
            if (fullClaims.containsKey(claimName)) {
                claims.put(claimName, fullClaims.get(claimName));
            }
        }
        return claims;
    }
}
