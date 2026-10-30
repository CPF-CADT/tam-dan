package com.tamdan.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class DemoDataInitializerTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(DemoDataInitializer.class);

    @Test
    void fixtureRequiresDevelopmentProfileAndExplicitProperty() {
        assertThat(DemoDataInitializer.class.getAnnotation(Profile.class).value()).containsExactly("dev");
        ConditionalOnProperty condition = DemoDataInitializer.class.getAnnotation(ConditionalOnProperty.class);
        assertThat(condition.name()).containsExactly("tamdan.demo-data.enabled");
        assertThat(condition.havingValue()).isEqualTo("true");
    }

    @Test
    void fixtureIsAbsentByDefault() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(DemoDataInitializer.class));
    }

    @Test
    void fixtureIsAbsentInProductionEvenWhenExplicitlyEnabled() {
        contextRunner
            .withPropertyValues("spring.profiles.active=prod", "tamdan.demo-data.enabled=true")
            .run(context -> assertThat(context).doesNotHaveBean(DemoDataInitializer.class));
    }
}
