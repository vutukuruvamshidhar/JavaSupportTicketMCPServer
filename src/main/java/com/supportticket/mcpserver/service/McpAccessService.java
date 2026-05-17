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
 *   2. Hold the {@code ticket_creator} realm role.
 *   3. Have the relevant role attribute set to {@code "true"} in the JWT.
 *
 * Role attributes are mapped into the JWT as top-level claims by a Keycloak
 * "Role Attribute" protocol mapper configured on the realm role.
 * Expected claim names: {@code access_tools}, {@code access_templates}, {@code access_prompts}.
 */
@Service
public class McpAccessService {

    private static final String TICKET_CREATOR_ROLE = "ticket_creator";

    public void requireToolAccess() {
        Jwt jwt = resolveJwt();
        checkRole(jwt);
        checkAttribute(jwt, "access_tools");
    }

    public void requireTemplateAccess() {
        Jwt jwt = resolveJwt();
        checkRole(jwt);
        checkAttribute(jwt, "access_templates");
    }

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
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null) {
            throw new AccessDeniedException("JWT is missing the realm_access claim.");
        }
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.get("roles");
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