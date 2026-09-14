package com.kardexis.auth.api;

import com.kardexis.auth.api.dto.AuthResponse;
import com.kardexis.auth.api.dto.LoginRequest;
import com.kardexis.auth.api.dto.RegisterRequest;
import com.kardexis.auth.api.dto.UserSummary;
import com.kardexis.auth.application.AuthService;
import com.kardexis.shared.config.JwtProperties;
import com.kardexis.shared.error.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;
    private final boolean cookieSecure;

    public AuthController(
            AuthService authService,
            JwtProperties jwtProperties,
            @Value("${app.cookie.secure:false}") boolean cookieSecure
    ) {
        this.authService = authService;
        this.jwtProperties = jwtProperties;
        this.cookieSecure = cookieSecure;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        AuthService.AuthResult result = authService.register(
                request.username(),
                request.email(),
                request.password(),
                request.displayName()
        );

        setRefreshTokenCookie(response, result.refreshToken());

        UserSummary userSummary = UserSummary.fromEntity(result.user());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(result.accessToken(), userSummary));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthService.AuthResult result = authService.login(request.email(), request.password());

        setRefreshTokenCookie(response, result.refreshToken());

        UserSummary userSummary = UserSummary.fromEntity(result.user());
        return ResponseEntity.ok(new AuthResponse(result.accessToken(), userSummary));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidCredentialsException("Refresh token cookie is missing");
        }

        AuthService.AuthResult result = authService.refresh(refreshToken);

        setRefreshTokenCookie(response, result.refreshToken());

        UserSummary userSummary = UserSummary.fromEntity(result.user());
        return ResponseEntity.ok(new AuthResponse(result.accessToken(), userSummary));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(0) // Expira la cookie inmediatamente
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(jwtProperties.refreshTokenDays()))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}