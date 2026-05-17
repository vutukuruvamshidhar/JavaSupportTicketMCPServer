package com.supportticket.mcpserver;

import com.supportticket.mcpserver.service.KeycloakUserService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * Test-only Spring configuration that stubs external dependencies so the full
 * application context can start without live external services.
 *
 * <p>{@link JwtDecoder} is replaced with a no-op mock to prevent
 * spring-boot-starter-oauth2-resource-server from fetching the Keycloak JWKS
 * endpoint at startup. {@link KeycloakUserService} is mocked so no real
 * Keycloak server is required for user-lookup calls.</p>
 */
@TestConfiguration
class TestConfig {

    @Bean
    @Primary
    KeycloakUserService keycloakUserService() {
        return Mockito.mock(KeycloakUserService.class);
    }

    /**
     * Overrides the auto-configured {@link JwtDecoder} so that the resource
     * server does not attempt to resolve the issuer URI (and fetch JWKS from
     * Keycloak) during context startup.
     */
    @Bean
    @Primary
    JwtDecoder jwtDecoder() {
        return Mockito.mock(JwtDecoder.class);
    }
}