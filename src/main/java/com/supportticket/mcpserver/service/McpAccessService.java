package com.supportticket.mcpserver.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Enforces fine-grained MCP access control based on the caller's JWT.
 *
 * The caller must:
 *   1. Be authenticated (a valid Bearer JWT must have been sent).
 *   2. Hold the {@code ticket_creator} client role defined on the
 *      {@code mcppocserver} Keycloak client (appears in the JWT under
 *      {@code resource_access.mcppocserver.roles}).
 *   3. Have the relevant role attribute set to {@code "true"} in the JWT.
 *
 * Role attributes are mapped into the JWT as top-level claims by a Keycloak
 * "Role Attribute" protocol mapper configured on the client role.
 * Expected claim names: {@code access_tools}, {@code access_templates}, {@code access_prompts}.
 */
@Service
public class McpAccessService {

    private static final String TICKET_CREATOR_ROLE = "ticket_creator";
    private static final String MCP_SERVER_CLIENT_ID = "mcppocserver";

    /**
     * Guards MCP tool invocations.
     *
     * <p>Verifies that the caller holds the {@code ticket_creator} role and has
     * the {@code access_tools} JWT attribute set to {@code "true"}.</p>
     *
     * @throws org.springframework.security.access.AccessDeniedException if the
     *         caller is not authenticated, lacks the required role, or the
     *         {@code access_tools} attribute is not {@code "true"}
     */
    public void requireToolAccess() {
        Jwt jwt = resolveJwt();
        checkRole(jwt);
        checkAttribute(jwt, "access_tools");
    }

    /**
     * Guards MCP resource (template) access.
     *
     * <p>Verifies that the caller holds the {@code ticket_creator} role and has
     * the {@code access_templates} JWT attribute set to {@code "true"}.</p>
     *
     * @throws org.springframework.security.access.AccessDeniedException if the
     *         caller is not authenticated, lacks the required role, or the
     *         {@code access_templates} attribute is not {@code "true"}
     */
    public void requireTemplateAccess() {
        Jwt jwt = resolveJwt();
        checkRole(jwt);
        checkAttribute(jwt, "access_templates");
    }

    /**
     * Guards MCP prompt access.
     *
     * <p>Verifies that the caller holds the {@code ticket_creator} role and has
     * the {@code access_prompts} JWT attribute set to {@code "true"}.</p>
     *
     * @throws org.springframework.security.access.AccessDeniedException if the
     *         caller is not authenticated, lacks the required role, or the
     *         {@code access_prompts} attribute is not {@code "true"}
     */
    public void requirePromptAccess() {
        Jwt jwt = resolveJwt();
        checkRole(jwt);
        checkAttribute(jwt, "access_prompts");
    }

    // -------------------------------------------------------------------------

    private Jwt resolveJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getToken();
        }
        throw new AccessDeniedException(
                "No authenticated user. A valid Bearer token is required to invoke MCP tools.");
    }

    private void checkRole(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess == null) {
            throw new AccessDeniedException("JWT is missing the resource_access claim.");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> clientAccess =
                (Map<String, Object>) resourceAccess.get(MCP_SERVER_CLIENT_ID);
        if (clientAccess == null) {
            throw new AccessDeniedException(
                    "JWT has no roles for Keycloak client '" + MCP_SERVER_CLIENT_ID + "'.");
        }
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) clientAccess.get("roles");
        if (roles == null || !roles.contains(TICKET_CREATOR_ROLE)) {
            throw new AccessDeniedException(
                    "User does not have the required '" + TICKET_CREATOR_ROLE + "' role.");
        }
    }

    private void checkAttribute(Jwt jwt, String attributeName) {
        Object value = jwt.getClaim(attributeName);
        if (!"true".equalsIgnoreCase(String.valueOf(value))) {
            throw new AccessDeniedException(
                    "The '" + attributeName + "' attribute on the ticket_creator role is not set to true.");
        }
    }
}