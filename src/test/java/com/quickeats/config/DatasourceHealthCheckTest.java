package com.quickeats.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class DatasourceHealthCheckTest {

    @Test
    void checkDatasourceUrl_WhenH2InNonDev_ExecutesWithoutError() {
        DatasourceHealthCheck check = new DatasourceHealthCheck();
        Environment env = mock(Environment.class);
        when(env.acceptsProfiles(Profiles.of("dev"))).thenReturn(false);

        ReflectionTestUtils.setField(check, "environment", env);
        ReflectionTestUtils.setField(check, "datasourceUrl", "jdbc:h2:mem:quickeatsdb");

        assertDoesNotThrow(check::checkDatasourceUrl);
    }

    @Test
    void checkDatasourceUrl_WhenPostgresInNonDev_ExecutesWithoutError() {
        DatasourceHealthCheck check = new DatasourceHealthCheck();
        Environment env = mock(Environment.class);
        when(env.acceptsProfiles(Profiles.of("dev"))).thenReturn(false);

        ReflectionTestUtils.setField(check, "environment", env);
        ReflectionTestUtils.setField(check, "datasourceUrl", "jdbc:postgresql://localhost:5432/quickeatsdb");

        assertDoesNotThrow(check::checkDatasourceUrl);
    }
}
