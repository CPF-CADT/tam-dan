package com.tamdan.service;

import java.time.Duration;

/** Atomic rate-limit storage contract for providers shared by all application instances. */
public interface RateLimitProvider {
    boolean tryAcquire(String key, int limit, Duration window);
}
