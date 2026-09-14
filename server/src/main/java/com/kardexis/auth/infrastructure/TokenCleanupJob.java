package com.kardexis.auth.infrastructure;

import com.kardexis.auth.infrastructure.persistence.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class TokenCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupJob.class);

    private final RefreshTokenRepository refreshTokenRepository;

    public TokenCleanupJob(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Purga tokens caducados o revocados diariamente a las 3:00 AM.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpiredTokens() {
        log.info("Starting scheduled cleanup of expired refresh tokens");
        int deletedCount = refreshTokenRepository.deleteAllExpiredOrRevoked(Instant.now());
        log.info("Finished cleanup of expired refresh tokens. Total purged: {}", deletedCount);
    }
}