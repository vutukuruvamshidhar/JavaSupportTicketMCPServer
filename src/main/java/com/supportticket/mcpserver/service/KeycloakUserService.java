package com.supportticket.mcpserver.service;

import com.supportticket.mcpserver.dto.Assignee;
import com.supportticket.mcpserver.dto.KeycloakTokenResponse;
import com.supportticket.mcpserver.dto.KeycloakUserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Service that queries the Keycloak Admin REST API to look up realm users.
 *
 * <p>Uses the OAuth 2.0 client credentials flow to obtain a short-lived admin
 * access token, then calls {@code GET /admin/realms/{realm}/users} with a
 * {@code search} parameter. The caller is responsible for providing Keycloak
 * coordinates via application properties.</p>
 */
@Service
public class KeycloakUserService {

    private static final Logger log = LoggerFactory.getLogger(KeycloakUserService.class);

    private final String serverUrl;
    private final String realm;
    private final String clientId;
    private final String clientSecret;
    private final RestTemplate restTemplate;

    /**
     * Creates the service with the given Keycloak connection parameters.
     *
     * @param serverUrl    base URL of the Keycloak server (e.g. {@code http://localhost:8080})
     * @param realm        the Keycloak realm to query
     * @param clientId     client ID used for the client-credentials token request
     * @param clientSecret client secret for the client-credentials token request
     * @param restTemplate {@link RestTemplate} qualified as {@code keycloakRestTemplate}
     */
    public KeycloakUserService(
            @Value("${keycloak.server-url}") String serverUrl,
            @Value("${keycloak.realm}") String realm,
            @Value("${keycloak.client-id}") String clientId,
            @Value("${keycloak.client-secret}") String clientSecret,
            @Qualifier("keycloakRestTemplate") RestTemplate restTemplate) {
        this.serverUrl = serverUrl;
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restTemplate = restTemplate;
    }

    /**
     * Searches Keycloak for users whose first name matches the given value.
     * Returns each match as an {@link Assignee}.
     */
    public List<Assignee> findUsersByFirstName(String firstName) {
        String token = getAccessToken();

        String url = UriComponentsBuilder
                .fromHttpUrl(serverUrl + "/admin/realms/{realm}/users")
                .queryParam("search", firstName)
                .buildAndExpand(realm)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        log.debug("Querying Keycloak users by firstName={}", firstName);

        ResponseEntity<KeycloakUserRepresentation[]> response = restTemplate.exchange(
                url, HttpMethod.GET, request, KeycloakUserRepresentation[].class);

        KeycloakUserRepresentation[] body = response.getBody();
        if (body == null || body.length == 0) {
            return Collections.emptyList();
        }

        return Arrays.stream(body)
                .map(u -> new Assignee(u.getId(), u.getDisplayName(), u.getEmail()))
                .toList();
    }

    private String getAccessToken() {
        String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        KeycloakTokenResponse response = restTemplate.postForObject(tokenUrl, request, KeycloakTokenResponse.class);

        if (response == null || response.getAccessToken() == null) {
            throw new IllegalStateException("Failed to obtain access token from Keycloak");
        }

        return response.getAccessToken();
    }
}