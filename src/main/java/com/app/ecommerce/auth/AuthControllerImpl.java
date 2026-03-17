package com.app.ecommerce.auth;

import com.app.ecommerce.shared.dto.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthControllerImpl implements AuthController {

    private final AuthService authService;

    @Override
    public ResponseEntity<ApiResponseDto<LoginResponse>> register(RegisterRequest request) {
        log.info("register({})", request.getUsername());
        LoginResponse loginResponse = authService.register(request);
        return new ResponseEntity<>(
                ApiResponseDto.created(loginResponse),
                HttpStatus.CREATED
        );
    }

    @Override
    public ResponseEntity<ApiResponseDto<LoginResponse>> login(LoginRequest request) {
        log.info("login({})", request.getUsername());
        LoginResponse loginResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponseDto.success(loginResponse));
    }

    @Override
    public ResponseEntity<ApiResponseDto<LoginResponse>> refreshToken(RefreshTokenRequest request) {
        log.info("refreshToken()");
        LoginResponse loginResponse = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponseDto.success(loginResponse));
    }
}
