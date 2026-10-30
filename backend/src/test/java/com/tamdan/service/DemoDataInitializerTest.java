package com.tamdan.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;

class DemoDataInitializerTest {

    @Test
    void fixtureRequiresDevelopmentProfileAndExplicitProperty() {
        assertThat(DemoDataInitializer.class.getAnnotation(Profile.class).value()).containsExactly("dev");
        ConditionalOnProperty condition = DemoDataInitializer.class.getAnnotation(ConditionalOnProperty.class);
        assertThat(condition.name()).containsExactly("tamdan.demo-data.enabled");
        assertThat(condition.havingValue()).isEqualTo("true");
    }
}
