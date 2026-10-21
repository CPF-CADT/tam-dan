package com.tamdan.entities;

import static com.tamdan.entities.AuthorityTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuthorityTest {

    @Test
    void authoritiesWithSameNameAreEqual() {
        Authority authority1 = getAuthoritySample1();
        Authority authority2 = Authority.named("different");
        assertThat(authority1).isNotEqualTo(authority2);

        authority2 = Authority.named(authority1.getName());
        assertThat(authority1).isEqualTo(authority2);
    }

    @Test
    void authoritiesWithSameNameHaveSameHashCode() {
        Authority authority1 = getAuthoritySample1();
        Authority authority2 = Authority.named(authority1.getName());
        assertThat(authority2).hasSameHashCodeAs(authority1);
    }
}
