package com.tamdan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tamdan.config.RateLimitProperties;
import com.tamdan.exception.RateLimitExceededException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class RateLimitServiceTest {

    @Test
    void allowsRequestsBelowLimit() {
        FakeProvider provider = new FakeProvider();
        RateLimitService service = service(provider, 3, Duration.ofMinutes(1));

        service.check(RateLimitService.Operation.LOGIN, "normalized-login");
        service.check(RateLimitService.Operation.LOGIN, "normalized-login");

        assertThat(provider.count("login:normalized-login")).isEqualTo(2);
    }

    @Test
    void rejectsAtLimit() {
        RateLimitService service = service(new FakeProvider(), 1, Duration.ofMinutes(1));

        service.check(RateLimitService.Operation.LOGIN, "same-login");

        assertThatThrownBy(() -> service.check(RateLimitService.Operation.LOGIN, "same-login"))
            .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void separateKeysHaveSeparateCounters() {
        FakeProvider provider = new FakeProvider();
        RateLimitService service = service(provider, 1, Duration.ofMinutes(1));

        service.check(RateLimitService.Operation.LOGIN, "first-login");
        service.check(RateLimitService.Operation.LOGIN, "second-login");

        assertThat(provider.count("login:first-login")).isEqualTo(1);
        assertThat(provider.count("login:second-login")).isEqualTo(1);
    }

    @Test
    void windowResetAllowsAnotherRequest() {
        FakeProvider provider = new FakeProvider();
        RateLimitService service = service(provider, 1, Duration.ofMillis(1));

        service.check(RateLimitService.Operation.LOGIN, "window-login");
        assertThatThrownBy(() -> service.check(RateLimitService.Operation.LOGIN, "window-login"))
            .isInstanceOf(RateLimitExceededException.class);
        provider.expire("login:window-login");
        service.check(RateLimitService.Operation.LOGIN, "window-login");
    }

    @Test
    void concurrentRequestsCannotExceedConfiguredLimit() throws Exception {
        FakeProvider provider = new FakeProvider();
        RateLimitService service = service(provider, 10, Duration.ofMinutes(1));
        var executor = Executors.newFixedThreadPool(8);
        var start = new CountDownLatch(1);
        var done = new CountDownLatch(20);
        for (int i = 0; i < 20; i++) {
            executor.submit(() -> {
                try {
                    start.await();
                    service.check(RateLimitService.Operation.PAIRING_SUBMISSION, "shared-submission");
                } catch (Exception ignored) {
                    // The assertion below counts successful acquisitions.
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        assertThat(done.await(2, TimeUnit.SECONDS)).isTrue();
        executor.shutdownNow();
        assertThat(provider.count("pairing_submission:shared-submission")).isEqualTo(0);
        assertThat(provider.count("pairing-submission:shared-submission")).isEqualTo(10);
    }

    @Test
    void loginFailuresUseTheSameThrottlingPath() {
        FakeProvider provider = new FakeProvider();
        RateLimitService service = service(provider, 2, Duration.ofMinutes(1));

        service.check(RateLimitService.Operation.LOGIN, "normalized-login");
        service.check(RateLimitService.Operation.LOGIN, "normalized-login");
        assertThatThrownBy(() -> service.check(RateLimitService.Operation.LOGIN, "normalized-login"))
            .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void pairingSubmissionAndRefreshUseIndependentOperations() {
        FakeProvider provider = new FakeProvider();
        RateLimitService service = service(provider, 1, Duration.ofMinutes(1));

        service.check(RateLimitService.Operation.PAIRING_SUBMISSION, "global");
        service.check(RateLimitService.Operation.REFRESH, "global");

        assertThat(provider.count("pairing-submission:global")).isEqualTo(1);
        assertThat(provider.count("refresh:global")).isEqualTo(1);
    }

    private RateLimitService service(FakeProvider provider, int limit, Duration window) {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setLogin(new RateLimitProperties.Rule(limit, window));
        properties.setPairingSubmission(new RateLimitProperties.Rule(limit, window));
        properties.setRefresh(new RateLimitProperties.Rule(limit, window));
        return new RateLimitService(provider, properties);
    }

    private static final class FakeProvider implements RateLimitProvider {
        private final Map<String, Window> windows = new ConcurrentHashMap<>();

        @Override
        public boolean tryAcquire(String key, int limit, Duration window) {
            Instant now = Instant.now();
            synchronized (windows) {
                Window current = windows.get(key);
                if (current == null || current.startedAt.plus(window).isBefore(now)) {
                    windows.put(key, new Window(now, 1));
                    return true;
                }
                if (current.count >= limit) return false;
                current.count++;
                return true;
            }
        }

        int count(String key) {
            Window window = windows.get(key);
            return window == null ? 0 : window.count;
        }

        void expire(String key) {
            Window window = windows.get(key);
            if (window != null) window.startedAt = Instant.EPOCH;
        }

        private static final class Window {
            private Instant startedAt;
            private int count;

            private Window(Instant startedAt, int count) {
                this.startedAt = startedAt;
                this.count = count;
            }
        }
    }
}
