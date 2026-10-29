package com.tamdan.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class SafetyPolicyTest {
    @Test
    void newPolicyStartsAsDraftAndVersionIncrementsOnUpdate() {
        ChildProfile child = org.mockito.Mockito.mock(ChildProfile.class);
        SafetyPolicy policy = SafetyPolicy.create(child, SafetyPolicy.Type.DOMAIN, "example.test", Instant.now());

        assertThat(policy.getState()).isEqualTo(SafetyPolicy.State.DRAFT);
        assertThat(policy.getVersion()).isOne();

        policy.update(SafetyPolicy.Type.APPLICATION, "browser");

        assertThat(policy.getVersion()).isEqualTo(2L);
        assertThat(policy.getState()).isEqualTo(SafetyPolicy.State.DRAFT);
    }
}
