package com.tamdan.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "rate_limit_bucket")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RateLimitBucket {
    @Id
    @Column(name = "rate_key", length = 64)
    private String rateKey;

    @Column(name = "window_started_at", nullable = false)
    private Instant windowStartedAt;

    @Column(name = "request_count", nullable = false)
    private Integer requestCount;

    public static RateLimitBucket create(String rateKey, Instant windowStartedAt) {
        return new RateLimitBucket(rateKey, windowStartedAt, 1);
    }
}
