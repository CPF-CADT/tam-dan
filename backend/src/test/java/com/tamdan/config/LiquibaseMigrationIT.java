package com.tamdan.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.tamdan.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
class LiquibaseMigrationIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void prd4ChangeSetsAreAppliedExactlyOnce() {
        List<String> ids = jdbcTemplate.queryForList(
            "select id from databasechangelog where id like '20261021%' order by id",
            String.class
        );

        assertThat(ids).containsExactly(
            "20261021000100",
            "20261021000200",
            "20261021000300",
            "20261021000400",
            "20261021000500",
            "20261021000600",
            "20261021000700",
            "20261021000800",
            "20261021000900"
        );
        assertThat(ids).doesNotHaveDuplicates();
    }

    @Test
    void prd4TablesAndHardeningIndexesExist() {
        assertThat(jdbcTemplate.queryForObject("select count(*) from information_schema.tables where table_name in ('family', 'family_member', 'child_profile', 'pairing_code', 'child_device', 'device_credential', 'security_audit_event')", Integer.class)).isEqualTo(7);
        List<String> indexes = jdbcTemplate.queryForList(
            "select indexname from pg_indexes where indexname in ('ix_family_member_active_parent_family', 'ix_child_profile_family', 'ix_pairing_code_active_child_expiry', 'ix_device_credential_active_hash_expiry', 'ix_security_audit_family_time')",
            String.class
        );
        assertThat(indexes).containsExactlyInAnyOrder(
            "ix_family_member_active_parent_family",
            "ix_child_profile_family",
            "ix_pairing_code_active_child_expiry",
            "ix_device_credential_active_hash_expiry",
            "ix_security_audit_family_time"
        );
    }
}
