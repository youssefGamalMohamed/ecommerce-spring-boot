package com.app.ecommerce.security.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.app.ecommerce.repository.TokenRepo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomLogoutHandler implements LogoutHandler {

    @Autowired
    private TokenRepo tokenRepo;


    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        jwt = authHeader.substring(7);
        var storedToken = tokenRepo.findByToken(jwt)
                .orElse(null);

        if(storedToken == null)
            throw new BadCredentialsException("token not exist to logout from system");

        storedToken.setExpired(true);
        storedToken.setRevoked(true);
        tokenRepo.save(storedToken);
        SecurityContextHolder.clearContext();

    }
}
