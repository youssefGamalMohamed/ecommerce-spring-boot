package com.app.ecommerce.service.impl;




import com.app.ecommerce.entity.Token;
import com.app.ecommerce.entity.User;
import com.app.ecommerce.enums.TokenType;
import com.app.ecommerce.exception.type.DuplicatedUniqueColumnValueException;
import com.app.ecommerce.exception.type.JsonParsingException;
import com.app.ecommerce.exception.type.MissingRefreshTokenException;
import com.app.ecommerce.factory.UserFactory;
import com.app.ecommerce.models.request.LoginRequestBody;
import com.app.ecommerce.models.request.RegisterRequestBody;
import com.app.ecommerce.models.response.endpoints.LoginResponseBody;
import com.app.ecommerce.models.response.endpoints.LogoutResponseBody;
import com.app.ecommerce.models.response.endpoints.RefreshTokenResponseBody;
import com.app.ecommerce.models.response.endpoints.RegisterResponseBody;
import com.app.ecommerce.mq.activemq.model.EmailQueueMessage;
import com.app.ecommerce.mq.activemq.sender.EmailQueueSender;
import com.app.ecommerce.repository.TokenRepo;
import com.app.ecommerce.repository.UserRepo;
import com.app.ecommerce.security.handler.CustomLogoutHandler;
import com.app.ecommerce.service.framework.IAuthenticationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {
    private final UserRepo userRepo;
    private final TokenRepo tokenRepo;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserFactory userFactory;

    private final CustomLogoutHandler logoutHandler;

    private final EmailQueueSender emailQueueSender;
    
    public RegisterResponseBody register(RegisterRequestBody registerRequestBody) throws JsonParsingException {

        if(userRepo.existsByEmail(registerRequestBody.getEmail()))
            throw new DuplicatedUniqueColumnValueException("Email Already Exist Before , Try another one");

        var user = userFactory.getUser(registerRequestBody);

        var savedUser = userRepo.save(user);
        var verificationToken = jwtService.generateVerificationToken(user);
        saveUserToken(savedUser, verificationToken , TokenType.VERIFICATION);
        
        // send message to EmailQueue to send to user email to great him/her
        emailQueueSender.sendToQueue(
                EmailQueueMessage.builder()
                        .email(user.getEmail())
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .verificationToken(verificationToken)
                        .build()
        );

        return RegisterResponseBody.builder()
                .accessToken("")
                .refreshToken("")
                .build();
    }

    public LoginResponseBody authenticate(LoginRequestBody request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepo.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken , TokenType.BEARER);
        return LoginResponseBody.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void saveUserToken(User user, String jwtToken , TokenType tokenType) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(tokenType)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepo.save(token);
    }

    public void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepo.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepo.saveAll(validUserTokens);
    }

    public RefreshTokenResponseBody refreshToken() throws IOException {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;

        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            throw new MissingRefreshTokenException("missing Refresh-Token in the Headers");
        }


        refreshToken = authHeader.substring(7);
        try {
            userEmail = jwtService.extractUsername(refreshToken);
        }
        catch (Exception exception) {
            throw new BadCredentialsException("corrupted or invalid Refresh-Token in the Headers");
        }



        if (userEmail != null) {
            var user = this.userRepo.findByEmail(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken , TokenType.BEARER);
                return RefreshTokenResponseBody.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();

            } else {
                throw new CredentialsExpiredException("expired refresh token");
            }
        }
        else {
            throw new BadCredentialsException("corrupted or invalid Refresh-Token in the Headers");
        }
    }


    public LogoutResponseBody logout() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();

        logoutHandler.logout(request , response , SecurityContextHolder.getContext().getAuthentication());

        return LogoutResponseBody.builder()
                .message("Logout Done Successfully")
                .build();
    }

    @Override
    public String verifyEmailByVerificationToken(String verificationToken) {
        var verificationTokenFromDB = tokenRepo.findByToken(verificationToken);
        if(!verificationTokenFromDB.isPresent()) {
            return "This Verification Token Not in DB";
        }
        if(jwtService.isTokenExpired(verificationTokenFromDB.get().getToken())) {
            return "verification token expired";
        }

        User user = verificationTokenFromDB.get().getUser();
        if(user.isEnabled()) {
            return "User Account Activated Before";
        }
        user.setEnabled(true);
        userRepo.save(user);

        return "Activated Successfully";
    }
}