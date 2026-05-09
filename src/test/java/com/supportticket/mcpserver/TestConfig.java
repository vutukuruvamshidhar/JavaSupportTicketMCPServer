package com.supportticket.mcpserver;

import com.supportticket.mcpserver.dto.Assignee;
import com.supportticket.mcpserver.service.AzureGraphClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Collections;
import java.util.List;

/**
 * Test-only Spring configuration that registers stub beans needed to start the
 * full application context in {@code @SpringBootTest} tests without real
 * external dependencies.
 */
@TestConfiguration
class TestConfig {

    /**
     * Provides a no-op {@link AzureGraphClient} bean when no real implementation
     * is present (i.e. Azure credentials are not configured in the test environment).
     *
     * @return a stub that always returns an empty list
     */
    @Bean
    @ConditionalOnMissingBean(AzureGraphClient.class)
    AzureGraphClient stubAzureGraphClient() {
        return displayName -> Collections.emptyList();
    }
}
