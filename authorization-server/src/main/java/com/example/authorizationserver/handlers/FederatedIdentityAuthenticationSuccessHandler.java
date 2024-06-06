package com.example.authorizationserver.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Consumer;

// this class handles and successfully login operaion from ME or GITHUB , GOOGLE and delegate user to
// user service to try to save it to db if user not exist
@Component
@Log4j2
public class FederatedIdentityAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthenticationSuccessHandler delegate;

    @Autowired
    private Consumer<OAuth2User> oauth2UserHandler;

    private Consumer<OidcUser> oidcUserHandler;

    public FederatedIdentityAuthenticationSuccessHandler() {
        delegate = new SavedRequestAwareAuthenticationSuccessHandler();
    }



    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException, ServletException, IOException {
        oidcUserHandler = this.oauth2UserHandler::accept;

        // to check if the token from oauth2
        if (authentication instanceof OAuth2AuthenticationToken) {
            if (authentication.getPrincipal() instanceof OidcUser) {
                this.oidcUserHandler.accept((OidcUser) authentication.getPrincipal());
            } else if (authentication.getPrincipal() instanceof OAuth2User) {
                this.oauth2UserHandler.accept((OAuth2User) authentication.getPrincipal());
            }
        }

        this.delegate.onAuthenticationSuccess(request, response, authentication);
    }


}
