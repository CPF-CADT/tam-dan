package com.tamdan.service;

import com.tamdan.exception.RateLimitExceededException;
import java.time.Duration;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RateLimitService {
    private final RateLimitProvider provider;
    private final RateLimitPolicy policy;

    public void check(Operation operation, String normalizedIdentity) {
        RateLimitPolicy.Rule rule = policy.rule(operation);
        String key = operation.name().toLowerCase(Locale.ROOT).replace('_', '-') + ":" + normalizedIdentity;
        if (!provider.tryAcquire(key, rule.limit(), rule.window())) throw new RateLimitExceededException("Rate limit exceeded");
    }

    /** Compatibility entry point for isolated tests with an explicit policy. */
    public void check(String key, int limit, Duration window) {
        if (!provider.tryAcquire(key, limit, window)) throw new RateLimitExceededException("Rate limit exceeded");
    }

    public enum Operation {
        REGISTRATION,
        LOGIN,
        RECOVERY,
        ACTIVATION_RESEND,
        PAIRING_GENERATION,
        PAIRING_SUBMISSION,
        REFRESH,
    }
}
