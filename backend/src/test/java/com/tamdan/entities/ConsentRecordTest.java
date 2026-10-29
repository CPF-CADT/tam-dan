package com.tamdan.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class ConsentRecordTest {

    @Test
    void storesConfirmationTimestampAlongsideDeviceConfirmation() {
        Instant acknowledgedAt = Instant.parse("2026-10-30T03:00:00Z");
        Instant confirmedAt = Instant.parse("2026-10-30T03:04:05Z");
        Family family = Family.create(acknowledgedAt);
        ChildProfile child = ChildProfile.create(family, "Consent child", acknowledgedAt);
        ConsentRecord record = ConsentRecord.create(family, child, "v1", "PARENT", 1L, acknowledgedAt, "GRANTED");

        record.confirmByDevice(confirmedAt);

        assertThat(record.getConfirmedByDevice()).isTrue();
        assertThat(record.getConfirmedAt()).isEqualTo(confirmedAt);
    }
}
