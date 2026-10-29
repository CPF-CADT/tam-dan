package com.tamdan.entities;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class PolicyDeliveryTest {
    @Test
    void staleAcknowledgementCannotOverwritePolicyVersion() {
        ChildProfile child = org.mockito.Mockito.mock(ChildProfile.class);
        ChildDevice device = org.mockito.Mockito.mock(ChildDevice.class);
        SafetyPolicy policy = SafetyPolicy.create(child, SafetyPolicy.Type.DOMAIN, "example.test", Instant.now());
        PolicyDelivery delivery = PolicyDelivery.pending(policy, device);

        assertThatThrownBy(() -> delivery.acknowledge(0L, PolicyDelivery.State.ACTIVE, Instant.now(), null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
