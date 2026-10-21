package com.tamdan.service;

import java.time.Duration;

public interface RateLimitPolicy {
    Rule rule(RateLimitService.Operation operation);

    default int pairingMaxAttempts() {
        return 5;
    }

    default int failedLoginMaxAttempts() {
        return 5;
    }

    default java.time.Duration loginLockDuration() {
        return java.time.Duration.ofMinutes(15);
    }

    record Rule(int limit, Duration window) {}
}
