package com.tamdan.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import com.tamdan.dto.request.ConsentDisclosureConfirmation;
import com.tamdan.security.AuthenticatedDeviceIdResolver;
import com.tamdan.security.AuthenticatedUserIdResolver;
import com.tamdan.service.consent.ConsentService;
import java.time.Instant;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class V1ConsentResourceTest {

    @Mock
    private ConsentService consentService;

    @Mock
    private AuthenticatedUserIdResolver authenticatedUserIdResolver;

    @Mock
    private AuthenticatedDeviceIdResolver authenticatedDeviceIdResolver;

    @InjectMocks
    private V1ConsentResource resource;

    @Test
    void forwardsConfirmedAtToConsentService() {
        Instant confirmedAt = Instant.parse("2026-10-30T03:04:05Z");
        when(authenticatedDeviceIdResolver.requireAuthenticatedDeviceId()).thenReturn(7L);
        ConsentDisclosureConfirmation request = new ConsentDisclosureConfirmation("v1", confirmedAt);

        resource.confirm(request);

        verify(consentService).confirmDisclosure(7L, "v1", confirmedAt);
    }

    @Test
    void requiresConfirmedAt() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        assertThat(validator.validate(new ConsentDisclosureConfirmation("v1", null)))
            .extracting(violation -> violation.getPropertyPath().toString())
            .containsExactly("confirmedAt");
    }
}
