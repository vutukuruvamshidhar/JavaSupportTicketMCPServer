package com.supportticket.mcpserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Minimal representation of the token response returned by the Keycloak
 * token endpoint ({@code /protocol/openid-connect/token}).
 *
 * <p>Only the fields required by {@link com.supportticket.mcpserver.service.KeycloakUserService}
 * are mapped; all other fields are silently ignored.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakTokenResponse {

    /** The OAuth 2.0 access token value. */
    @JsonProperty("access_token")
    private String accessToken;

    /** The token type (typically {@code "Bearer"}). */
    @JsonProperty("token_type")
    private String tokenType;

    /** Lifetime of the access token in seconds. */
    @JsonProperty("expires_in")
    private int expiresIn;

    /**
     * Returns the access token value.
     *
     * @return the access token, or {@code null} if the response did not include one
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Sets the access token value.
     *
     * @param accessToken the access token to store
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}