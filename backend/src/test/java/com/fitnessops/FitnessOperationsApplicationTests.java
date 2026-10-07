package com.fitnessops;

import static org.assertj.core.api.Assertions.assertThat;

import com.fitnessops.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class FitnessOperationsApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoadsAndMigrationsSeedStaffRoles() {
        assertThat(jdbc.queryForList("select code from roles order by sort_order", String.class))
                .containsExactly("CHAIN_OWNER", "CLUB_MANAGER", "SALES_CONSULTANT", "RECEPTIONIST",
                        "PERSONAL_TRAINER", "GROUP_TRAINER", "ACCOUNTANT", "TECHNICIAN", "MEMBER_CARE", "ADMIN");
        assertThat(jdbc.queryForObject("select requires_counter_device from roles where code = 'RECEPTIONIST'",
                Boolean.class)).isTrue();
    }
}
