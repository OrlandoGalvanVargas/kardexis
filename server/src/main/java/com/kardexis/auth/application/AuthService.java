package com.kardexis.auth.application;

import com.kardexis.auth.domain.RefreshToken;
import com.kardexis.auth.domain.User;
import com.kardexis.auth.domain.UserAlreadyExistsException;
import com.kardexis.auth.infrastructure.persistence.RefreshTokenRepository;
import com.kardexis.auth.infrastructure.persistence.UserRepository;
import com.kardexis.shared.config.JwtProperties;
import com.kardexis.shared.error.InvalidCredentialsException;
import com.kardexis.shared.security.JwtTokenService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenService jwtTokenService,
                       JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public AuthResult register(String username, String email, String password, String displayName) {
        if (userRepository.existsByEmail(email)) {
            throw UserAlreadyExistsException.withEmail(email);
        }
        if (userRepository.existsByUsername(username)) {
            throw UserAlreadyExistsException.withUsername(username);
        }

        try {
            String passwordHash = passwordEncoder.encode(password);
            User user = new User(username, email, passwordHash, displayName);
            userRepository.save(user);

            return generateTokens(user);
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistsException("User with provided username or email already exists");
        }
    }

    @Transactional
    public AuthResult login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return generateTokens(user);
    }

    @Transactional
    public AuthResult refresh(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        if (!refreshToken.isActive()) {
            throw new InvalidCredentialsException("Refresh token is expired or revoked");
        }

        refreshToken.revoke();

        return generateTokens(refreshToken.getUser());
    }

    private AuthResult generateTokens(User user) {
        String accessToken = jwtTokenService.generateAccessToken(user.getId(), user.getUsername());
        String rawRefreshToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawRefreshToken);

        Instant expiresAt = Instant.now().plus(jwtProperties.refreshTokenDays(), ChronoUnit.DAYS);
        RefreshToken refreshToken = new RefreshToken(user, tokenHash, expiresAt);
        refreshTokenRepository.save(refreshToken);

        return new AuthResult(accessToken, rawRefreshToken, user);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available in the runtime environment", e);
        }
    }

    public record AuthResult(String accessToken, String refreshToken, User user) {
    }
}