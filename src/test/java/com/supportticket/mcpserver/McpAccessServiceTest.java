package com.supportticket.mcpserver;

import com.supportticket.mcpserver.service.McpAccessService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link McpAccessService}.
 *
 * Each test sets up the Spring Security context with a crafted JWT, invokes
 * one of the three guard methods, and asserts the expected outcome.
 * The context is cleared after every test to prevent state leakage.
 */
class McpAccessServiceTest {

    private McpAccessService service;

    @BeforeEach
    void setUp() {
        service = new McpAccessService();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // -----------------------------------------------------------------------
    // No / wrong authentication type
    // -----------------------------------------------------------------------

    @Test
    void requireToolAccess_throws_whenNoAuthentication() {
        // SecurityContext is empty – no authentication set
        assertThatThrownBy(service::requireToolAccess)
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("No authenticated user");
    }

    @Test
    void requireTemplateAccess_throws_whenNoAuthentication() {
        assertThatThrownBy(service::requireTemplateAccess)
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void requirePromptAccess_throws_whenNoAuthentication() {
        assertThatThrownBy(service::requirePromptAccess)
                .isInstanceOf(AccessDeniedException.class);
    }

    // -----------------------------------------------------------------------
    // Missing realm_access claim
    // -----------------------------------------------------------------------

    @Test
    void requireToolAccess_throws_whenRealmAccessClaimMissing() {
        authenticateWith(buildJwt(null, Map.of("access_tools", "true")));

        assertThatThrownBy(service::requireToolAccess)
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("realm_access");
    }

    // -----------------------------------------------------------------------
    // Missing or wrong role
    // -----------------------------------------------------------------------

    @Test
    void requireToolAccess_throws_whenTicketCreatorRoleMissing() {
        authenticateWith(buildJwt(List.of("some_other_role"), Map.of("access_tools", "true")));

        assertThatThrownBy(service::requireToolAccess)
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("ticket_creator");
    }

    // -----------------------------------------------------------------------
    // Role attribute not true
    // -----------------------------------------------------------------------

    @Test
    void requireToolAccess_throws_whenAccessToolsIsFalse() {
        authenticateWith(buildJwt(List.of("ticket_creator"), Map.of("access_tools", "false")));

        assertThatThrownBy(service::requireToolAccess)
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("access_tools");
    }

    @Test
    void requireToolAccess_throws_whenAccessToolsClaimAbsent() {
        authenticateWith(buildJwt(List.of("ticket_creator"), Map.of()));

        assertThatThrownBy(service::requireToolAccess)
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("access_tools");
    }

    @Test
    void requireTemplateAccess_throws_whenAccessTemplatesIsFalse() {
        authenticateWith(buildJwt(List.of("ticket_creator"), Map.of("access_templates", "false")));

        assertThatThrownBy(service::requireTemplateAccess)
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("access_templates");
    }

    @Test
    void requirePromptAccess_throws_whenAccessPromptsIsFalse() {
        authenticateWith(buildJwt(List.of("ticket_creator"), Map.of("access_prompts", "false")));

        assertThatThrownBy(service::requirePromptAccess)
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("access_prompts");
    }

    // -----------------------------------------------------------------------
    // Happy path
    // -----------------------------------------------------------------------

    @Test
    void requireToolAccess_succeeds_whenRoleAndAttributePresent() {
        authenticateWith(buildJwt(List.of("ticket_creator"), Map.of("access_tools", "true")));

        assertThatCode(service::requireToolAccess).doesNotThrowAnyException();
    }

    @Test
    void requireTemplateAccess_succeeds_whenRoleAndAttributePresent() {
        authenticateWith(buildJwt(List.of("ticket_creator"), Map.of("access_templates", "true")));

        assertThatCode(service::requireTemplateAccess).doesNotThrowAnyException();
    }

    @Test
    void requirePromptAccess_succeeds_whenRoleAndAttributePresent() {
        authenticateWith(buildJwt(List.of("ticket_creator"), Map.of("access_prompts", "true")));

        assertThatCode(service::requirePromptAccess).doesNotThrowAnyException();
    }

    @Test
    void requireToolAccess_succeeds_whenAttributeValueIsTrueUppercase() {
        authenticateWith(buildJwt(List.of("ticket_creator"), Map.of("access_tools", "TRUE")));

        assertThatCode(service::requireToolAccess).doesNotThrowAnyException();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Jwt buildJwt(List<String> roles, Map<String, Object> extraClaims) {
        Jwt.Builder builder = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject("test-user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300));

        if (roles != null) {
            builder.claim("realm_access", Map.of("roles", roles));
        }
        extraClaims.forEach(builder::claim);
        return builder.build();
    }

    private void authenticateWith(Jwt jwt) {
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}