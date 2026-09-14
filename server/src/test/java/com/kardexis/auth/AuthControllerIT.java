package com.kardexis.auth;

import com.kardexis.auth.api.dto.AuthResponse;
import com.kardexis.auth.api.dto.LoginRequest;
import com.kardexis.auth.api.dto.RegisterRequest;
import com.kardexis.auth.infrastructure.persistence.RefreshTokenRepository;
import com.kardexis.auth.infrastructure.persistence.UserRepository;
import com.kardexis.support.PostgresTestContainerBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerIT extends PostgresTestContainerBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String LOGIN_URL = "/api/v1/auth/login";
    private static final String REFRESH_URL = "/api/v1/auth/refresh";

    private RegisterRequest validRegisterRequest;

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos antes de cada test para garantizar aislamiento
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        validRegisterRequest = new RegisterRequest(
                "testuser",
                "test@example.com",
                "securePassword123",
                "Test User"
        );
    }

    @Test
    @DisplayName("Should register a new user and return 201 with auth response and refresh cookie")
    void shouldRegisterNewUser() {
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                REGISTER_URL, validRegisterRequest, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();
        assertThat(response.getBody().user()).isNotNull();
        assertThat(response.getBody().user().username()).isEqualTo("testuser");
        assertThat(response.getBody().user().email()).isEqualTo("test@example.com");

        // Verificación de la cookie de refresh token
        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeader).isNotNull();
        assertThat(setCookieHeader).contains("refresh_token");
        assertThat(setCookieHeader).contains("HttpOnly");
    }

    @Test
    @DisplayName("Should return 409 when registering with an existing email")
    void shouldRejectDuplicateEmail() {
        restTemplate.postForEntity(REGISTER_URL, validRegisterRequest, AuthResponse.class);

        RegisterRequest duplicateEmailRequest = new RegisterRequest(
                "anotheruser",
                "test@example.com",
                "anotherPassword123",
                "Another User"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                REGISTER_URL, duplicateEmailRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Should return 409 when registering with an existing username")
    void shouldRejectDuplicateUsername() {
        restTemplate.postForEntity(REGISTER_URL, validRegisterRequest, AuthResponse.class);

        RegisterRequest duplicateUsernameRequest = new RegisterRequest(
                "testuser",
                "another@example.com",
                "anotherPassword123",
                "Another User"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                REGISTER_URL, duplicateUsernameRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Should return 400 for invalid registration data")
    void shouldRejectInvalidRegistration() {
        RegisterRequest invalidRequest = new RegisterRequest(
                "",
                "not-an-email",
                "short",
                null
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                REGISTER_URL, invalidRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should login successfully and return an access token")
    void shouldLoginSuccessfully() {
        restTemplate.postForEntity(REGISTER_URL, validRegisterRequest, AuthResponse.class);

        LoginRequest loginRequest = new LoginRequest("test@example.com", "securePassword123");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                LOGIN_URL, loginRequest, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();
        assertThat(response.getBody().tokenType()).isEqualTo("Bearer");

        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeader).contains("refresh_token");
    }

    @Test
    @DisplayName("Should return 401 for login with wrong password")
    void shouldRejectLoginWithWrongPassword() {
        restTemplate.postForEntity(REGISTER_URL, validRegisterRequest, AuthResponse.class);

        LoginRequest loginRequest = new LoginRequest("test@example.com", "wrongPassword");
        ResponseEntity<Map> response = restTemplate.postForEntity(
                LOGIN_URL, loginRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should return 401 for login with non-existent email")
    void shouldRejectLoginWithNonExistentEmail() {
        LoginRequest loginRequest = new LoginRequest("nonexistent@example.com", "somePassword");
        ResponseEntity<Map> response = restTemplate.postForEntity(
                LOGIN_URL, loginRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should refresh tokens with a valid refresh cookie")
    void shouldRefreshTokens() {
        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
                REGISTER_URL, validRegisterRequest, AuthResponse.class);
        String refreshCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, refreshCookie);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<AuthResponse> response = restTemplate.exchange(
                REFRESH_URL, HttpMethod.POST, request, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();

        String newCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(newCookie).isNotNull();
        assertThat(newCookie).isNotEqualTo(refreshCookie);
    }

    @Test
    @DisplayName("Should return 401 when refreshing with an invalid cookie")
    void shouldRejectInvalidRefreshToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "refresh_token=invalid-token");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                REFRESH_URL, HttpMethod.POST, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}