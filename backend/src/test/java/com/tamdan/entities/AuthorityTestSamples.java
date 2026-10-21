package com.tamdan.entities;

import java.util.UUID;

public class AuthorityTestSamples {

    public static Authority getAuthoritySample1() {
        return Authority.named("name1");
    }

    public static Authority getAuthoritySample2() {
        return Authority.named("name2");
    }

    public static Authority getAuthorityRandomSampleGenerator() {
        return Authority.named(UUID.randomUUID().toString());
    }
}
