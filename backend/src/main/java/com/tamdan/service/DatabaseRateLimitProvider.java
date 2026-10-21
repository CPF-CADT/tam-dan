package com.tamdan.service;

import com.tamdan.repository.RateLimitBucketRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DatabaseRateLimitProvider implements RateLimitProvider {
    private final RateLimitBucketRepository repository;

    @Override
    @Transactional
    public boolean tryAcquire(String key, int limit, Duration window) {
        String hashedKey = hash(key);
        Instant now = Instant.now();
        Instant windowStart = now.minus(window);
        if (repository.incrementIfAvailable(hashedKey, windowStart, limit) == 1) return true;
        if (repository.resetIfExpired(hashedKey, now, windowStart) == 1) return true;
        if (repository.insertIfAbsent(hashedKey, now) == 1) return true;
        return repository.incrementIfAvailable(hashedKey, windowStart, limit) == 1;
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
