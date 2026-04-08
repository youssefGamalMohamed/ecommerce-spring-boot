package com.app.ecommerce.auth;

import com.app.ecommerce.shared.exception.DuplicatedUniqueColumnValueException;
import com.app.ecommerce.shared.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuthMapper authMapper;

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicatedUniqueColumnValueException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicatedUniqueColumnValueException("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = authMapper.mapToUser(request, encodedPassword);
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        Token token = Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(user)
                .revoked(false)
                .expired(false)
                .build();

        tokenRepository.save(token);

        log.info("User registered successfully: {}", user.getUsername());
        return authMapper.mapToLoginResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Logging in user: {}", request.getUsername());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            userRepository.findByUsername(request.getUsername())
                    .ifPresent(user -> userRepository.incrementFailedLoginAttempts(user.getId()));
            log.warn("Failed login attempt for user: {}", request.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();
        userRepository.resetFailedLoginAttempts(user.getId());

        tokenRepository.revokeAllValidTokensByUser(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        Token newToken = Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(user)
                .revoked(false)
                .expired(false)
                .build();

        tokenRepository.save(newToken);

        log.info("User logged in successfully: {}", user.getUsername());
        return authMapper.mapToLoginResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing token");

        Token token = tokenRepository.findByRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (token.isRevoked() || token.isExpired()) {
            throw new IllegalArgumentException("Refresh token is revoked or expired");
        }

        String username = jwtService.extractUsername(token.getRefreshToken());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!jwtService.isRefreshTokenValid(token.getRefreshToken(), user)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        token.setRevoked(true);
        tokenRepository.save(token);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        Token newToken = Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(user)
                .revoked(false)
                .expired(false)
                .build();

        tokenRepository.save(newToken);

        log.info("Token refreshed successfully for user: {}", user.getUsername());
        return authMapper.mapToLoginResponse(accessToken, refreshToken);
    }
}
