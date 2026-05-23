package com.supportticket.mcpserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Configures the MCP server as an OAuth 2.1 Resource Server.
 *
 * All MCP endpoints permit unauthenticated HTTP-level access so that the MCP
 * client can complete the protocol handshake (SSE stream + initialize call)
 * at startup before any user token is available. Fine-grained access control
 * is enforced at the tool / resource / prompt level by {@link McpAccessService},
 * which inspects the JWT that the client forwards at runtime.
 *
 * Spring Security still validates every Bearer token that IS present, populating
 * the SecurityContext so that McpAccessService can read the claims.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures HTTP security for the MCP server.
     *
     * <p>All requests are permitted at the HTTP level so that the MCP client can
     * complete the startup handshake before any user token is available.
     * OAuth 2.1 JWT validation is still active — any Bearer token that is present
     * is validated and the resulting authentication is stored in the
     * {@link org.springframework.security.core.context.SecurityContext} for use by
     * {@link com.supportticket.mcpserver.service.McpAccessService}.</p>
     *
     * @param http the {@link HttpSecurity} builder provided by Spring Security
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the security configuration cannot be built
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtConverter()))
            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    /**
     * Converts Keycloak client roles into Spring Security
     * {@code ROLE_<name>} granted authorities.
     *
     * <p>Reads roles from {@code resource_access.mcppocserver.roles} in the JWT —
     * the standard location for client-scoped roles assigned on the
     * {@code mcppocserver} Keycloak client. The resulting authorities are available
     * via {@code @PreAuthorize} expressions and are also inspected manually by
     * {@link com.supportticket.mcpserver.service.McpAccessService}.</p>
     *
     * @return a configured {@link JwtAuthenticationConverter}
     */
    @Bean
    public JwtAuthenticationConverter keycloakJwtConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess == null) return Collections.emptyList();

            @SuppressWarnings("unchecked")
            Map<String, Object> clientAccess =
                    (Map<String, Object>) resourceAccess.get("mcppocserver");
            if (clientAccess == null) return Collections.emptyList();

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) clientAccess.get("roles");
            if (roles == null) return Collections.emptyList();

            return roles.stream()
                    .map(role -> (org.springframework.security.core.GrantedAuthority)
                            new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
        });
        return converter;
    }
}