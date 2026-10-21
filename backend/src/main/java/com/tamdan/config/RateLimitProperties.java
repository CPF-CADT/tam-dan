package com.tamdan.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import com.tamdan.service.RateLimitPolicy;
import com.tamdan.service.RateLimitService;

@Getter
@Setter
@ConfigurationProperties(prefix = "application.rate-limit")
public class RateLimitProperties implements RateLimitPolicy {
    private Rule registration = new Rule(5, Duration.ofHours(1));
    private Rule login = new Rule(10, Duration.ofMinutes(1));
    private Rule recovery = new Rule(5, Duration.ofHours(1));
    private Rule activationResend = new Rule(5, Duration.ofHours(1));
    private Rule pairingGeneration = new Rule(10, Duration.ofHours(1));
    private Rule pairingSubmission = new Rule(10, Duration.ofMinutes(10));
    private Rule refresh = new Rule(20, Duration.ofMinutes(1));
    private int pairingMaxAttempts = 5;
    private int failedLoginMaxAttempts = 5;
    private Duration loginLockDuration = Duration.ofMinutes(15);

    @Getter
    @Setter
    public static class Rule {
        private int limit;
        private Duration window;

        public Rule() {}

        public Rule(int limit, Duration window) {
            this.limit = limit;
            this.window = window;
        }
    }

    @Override
    public RateLimitPolicy.Rule rule(RateLimitService.Operation operation) {
        Rule configured = switch (operation) {
            case REGISTRATION -> registration;
            case LOGIN -> login;
            case RECOVERY -> recovery;
            case ACTIVATION_RESEND -> activationResend;
            case PAIRING_GENERATION -> pairingGeneration;
            case PAIRING_SUBMISSION -> pairingSubmission;
            case REFRESH -> refresh;
        };
        return new RateLimitPolicy.Rule(configured.limit, configured.window);
    }

    @Override
    public int pairingMaxAttempts() {
        return pairingMaxAttempts;
    }

    @Override
    public int failedLoginMaxAttempts() {
        return failedLoginMaxAttempts;
    }

    @Override
    public Duration loginLockDuration() {
        return loginLockDuration;
    }
}
