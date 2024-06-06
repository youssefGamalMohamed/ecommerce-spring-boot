package com.example.authorizationserver.handlers;

import com.example.authorizationserver.entities.AuthorityEntity;
import com.example.authorizationserver.entities.UserEntity;
import com.example.authorizationserver.services.UserDetailsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class SuccessOAuth2UserLoginHandler implements Consumer<OAuth2User> {

    private final Function<String,String> getKeyOfUserNameDepenedingOnRegisteredClient = (registeredClientId -> {
        if(registeredClientId.equals("google"))
            return "email";
        if(registeredClientId.equals("github"))
            return "login";
        return "NOT_DETERMINED";
    });;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserDetailsService userDetailsService;


    @Override
    public void accept(OAuth2User oAuth2User) {
        // this variable holds the name of the provider ( ME , GITHUB , GOOGLE , FACEBOOK , OKTA )
        String authProviderName = "AUTHORIZATION_SERVER_ME";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            authProviderName = oauth2Token.getAuthorizedClientRegistrationId();
            log.info(">>>>>> OAUTH2 PROVIDER =  " + oauth2Token.getAuthorizedClientRegistrationId());

        }


        // this line get username key in the response of the auth provider
        // let say that provider is github then it call the username with attribute called "login"
        // let say that provider is google then it call the username with attribute called "email"
        // then it get the value that related to the attribute from the response of the provider
        String username = oAuth2User.getAttribute(this.getKeyOfUserNameDepenedingOnRegisteredClient.apply(authProviderName));


        DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) oAuth2User;
        log.info("NNNNAEEEMMME = " + defaultOAuth2User.getAttributes().get(defaultOAuth2User.getName()));
        try {
            String claimJsonString = objectMapper.writeValueAsString(oAuth2User.getAttributes());
            UserEntity userEntity = UserEntity.builder()
                    .username(username)
                    .password("NO_PASSWORD_FOR_ME_I'M_FROM_OUTSIDE_PROVIDER")
                    .claims(claimJsonString)
                    .authProviderName(authProviderName)
                    .authorities(
                            Set.of(
                                    AuthorityEntity.builder()
                                            .name("ROLE_CUSTOMER") // by default any user from GOOGLE , GITHUB will be a ROLE_USER
                                            .build(),
                                    AuthorityEntity.builder()
                                            .name("READ") // by default any user from GOOGLE , GITHUB will be a ROLE_USER
                                            .build()
                            )
                    )
                    .build();
            log.info(">>>>> User: name=" + oAuth2User.getName() + ", claims=" + oAuth2User.getAttributes() + ", authorities=" + oAuth2User.getAuthorities());
            userDetailsService.saveUserIFNotExists(userEntity);

        } catch (JsonProcessingException e) {
            log.error(">>>>> Exception while converting object and try to save another provider user to our db");
            throw new RuntimeException(e);
        }
    }

}
