package com.tamdan.repository;

import com.tamdan.entities.RateLimitBucket;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RateLimitBucketRepository extends JpaRepository<RateLimitBucket, String> {
    @Modifying
    @Query(value = "update rate_limit_bucket set request_count = request_count + 1 where rate_key = :key and window_started_at > :windowStart and request_count < :limit", nativeQuery = true)
    int incrementIfAvailable(@Param("key") String key, @Param("windowStart") Instant windowStart, @Param("limit") int limit);

    @Modifying
    @Query(value = "update rate_limit_bucket set window_started_at = :now, request_count = 1 where rate_key = :key and window_started_at <= :windowStart", nativeQuery = true)
    int resetIfExpired(@Param("key") String key, @Param("now") Instant now, @Param("windowStart") Instant windowStart);

    @Modifying
    @Query(value = "insert into rate_limit_bucket (rate_key, window_started_at, request_count) values (:key, :now, 1) on conflict (rate_key) do nothing", nativeQuery = true)
    int insertIfAbsent(@Param("key") String key, @Param("now") Instant now);
}
