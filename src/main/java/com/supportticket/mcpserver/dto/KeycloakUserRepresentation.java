package com.supportticket.mcpserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Subset of the Keycloak Admin REST API user representation returned by
 * {@code GET /admin/realms/{realm}/users}.
 *
 * <p>Unknown fields are silently ignored so the DTO remains stable as the
 * Keycloak API evolves.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakUserRepresentation {

    /** Keycloak-internal UUID for the user. */
    private String id;

    /** Keycloak username (login name). */
    private String username;

    /** User's first name. */
    private String firstName;

    /** User's last name. */
    private String lastName;

    /** Primary email address. */
    private String email;

    /** Returns the Keycloak-internal UUID. */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    /** Returns the Keycloak username. */
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    /** Returns the first name. */
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    /** Returns the last name. */
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    /** Returns the primary email address. */
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns the full name as {@code "firstName lastName"}, trimmed.
     * Falls back to {@link #username} when both name fields are blank.
     *
     * @return a non-null display name
     */
    public String getDisplayName() {
        String fn = firstName != null ? firstName : "";
        String ln = lastName != null ? lastName : "";
        String full = (fn + " " + ln).trim();
        return full.isEmpty() ? username : full;
    }
}