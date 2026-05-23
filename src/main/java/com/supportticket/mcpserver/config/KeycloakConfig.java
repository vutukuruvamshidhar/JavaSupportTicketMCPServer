package com.supportticket.mcpserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Spring configuration for Keycloak administrative API access.
 *
 * <p>Provides a dedicated {@link RestTemplate} bean used exclusively by
 * {@link com.supportticket.mcpserver.service.KeycloakUserService} to call
 * the Keycloak Admin REST API. A named qualifier keeps this instance separate
 * from any other {@code RestTemplate} beans that may exist in the context.</p>
 */
@Configuration
public class KeycloakConfig {

    /**
     * Creates a plain {@link RestTemplate} for Keycloak Admin REST API calls.
     *
     * <p>The qualifier {@code keycloakRestTemplate} is required by
     * {@link com.supportticket.mcpserver.service.KeycloakUserService}.</p>
     *
     * @return a new {@link RestTemplate} instance
     */
    @Bean
    public RestTemplate keycloakRestTemplate() {
        return new RestTemplate();
    }
}