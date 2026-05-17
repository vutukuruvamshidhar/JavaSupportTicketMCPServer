package com.supportticket.mcpserver;

import com.supportticket.mcpserver.dto.Assignee;
import com.supportticket.mcpserver.dto.KeycloakTokenResponse;
import com.supportticket.mcpserver.dto.KeycloakUserRepresentation;
import com.supportticket.mcpserver.service.KeycloakUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link KeycloakUserService}.
 *
 * <p>Uses a mocked {@link RestTemplate} so no real Keycloak server is required.
 * Covers: single match, multiple matches, no match, and token failure.</p>
 */
@ExtendWith(MockitoExtension.class)
class KeycloakUserServiceTest {

    private static final String SERVER_URL = "http://localhost:8080";
    private static final String REALM = "test-realm";
    private static final String CLIENT_ID = "test-client";
    private static final String CLIENT_SECRET = "test-secret";

    @Mock
    private RestTemplate restTemplate;

    private KeycloakUserService service;

    @BeforeEach
    void setUp() {
        service = new KeycloakUserService(SERVER_URL, REALM, CLIENT_ID, CLIENT_SECRET, restTemplate);
    }

    // -----------------------------------------------------------------------
    // Token retrieval
    // -----------------------------------------------------------------------

    @Test
    void findUsersByFirstName_throwsWhenTokenResponseIsNull() {
        when(restTemplate.postForObject(anyString(), any(), eq(KeycloakTokenResponse.class)))
                .thenReturn(null);

        assertThatThrownBy(() -> service.findUsersByFirstName("Alice"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to obtain access token");
    }

    @Test
    void findUsersByFirstName_throwsWhenAccessTokenIsNull() {
        KeycloakTokenResponse emptyResponse = new KeycloakTokenResponse();
        when(restTemplate.postForObject(anyString(), any(), eq(KeycloakTokenResponse.class)))
                .thenReturn(emptyResponse);

        assertThatThrownBy(() -> service.findUsersByFirstName("Alice"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to obtain access token");
    }

    // -----------------------------------------------------------------------
    // Single match
    // -----------------------------------------------------------------------

    @Test
    void findUsersByFirstName_returnsSingleAssignee() {
        stubToken("tok-123");

        KeycloakUserRepresentation user = keycloakUser("id-001", "Alice", "Smith", "alice@example.com");
        stubUserSearch(new KeycloakUserRepresentation[]{user});

        List<Assignee> result = service.findUsersByFirstName("Alice");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("id-001");
        assertThat(result.get(0).getDisplayName()).isEqualTo("Alice Smith");
        assertThat(result.get(0).getEmail()).isEqualTo("alice@example.com");
    }

    // -----------------------------------------------------------------------
    // Multiple matches
    // -----------------------------------------------------------------------

    @Test
    void findUsersByFirstName_returnsMultipleAssignees() {
        stubToken("tok-456");

        KeycloakUserRepresentation u1 = keycloakUser("id-001", "Joe", "Smith", "joe.smith@example.com");
        KeycloakUserRepresentation u2 = keycloakUser("id-002", "Joe", "Bloggs", "joe.bloggs@example.com");
        stubUserSearch(new KeycloakUserRepresentation[]{u1, u2});

        List<Assignee> result = service.findUsersByFirstName("Joe");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Assignee::getDisplayName)
                .containsExactlyInAnyOrder("Joe Smith", "Joe Bloggs");
    }

    // -----------------------------------------------------------------------
    // No match
    // -----------------------------------------------------------------------

    @Test
    void findUsersByFirstName_returnsEmptyListWhenNoMatch() {
        stubToken("tok-789");
        stubUserSearch(new KeycloakUserRepresentation[0]);

        List<Assignee> result = service.findUsersByFirstName("Unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void findUsersByFirstName_returnsEmptyListWhenBodyIsNull() {
        stubToken("tok-789");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(KeycloakUserRepresentation[].class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        List<Assignee> result = service.findUsersByFirstName("Unknown");

        assertThat(result).isEmpty();
    }

    // -----------------------------------------------------------------------
    // Admin API URL construction
    // -----------------------------------------------------------------------

    @Test
    void findUsersByFirstName_callsCorrectAdminUrl() {
        stubToken("tok-abc");
        stubUserSearch(new KeycloakUserRepresentation[0]);

        service.findUsersByFirstName("Alice");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(
                urlCaptor.capture(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(KeycloakUserRepresentation[].class));

        assertThat(urlCaptor.getValue())
                .contains("/admin/realms/" + REALM + "/users")
                .contains("search=Alice");
    }

    // -----------------------------------------------------------------------
    // Display name fallback (no first/last name — use username)
    // -----------------------------------------------------------------------

    @Test
    void findUsersByFirstName_usesUsernameWhenNamesAreMissing() {
        stubToken("tok-xyz");

        KeycloakUserRepresentation user = new KeycloakUserRepresentation();
        user.setId("id-999");
        user.setUsername("jdoe");
        user.setEmail("jdoe@example.com");
        stubUserSearch(new KeycloakUserRepresentation[]{user});

        List<Assignee> result = service.findUsersByFirstName("jdoe");

        assertThat(result.get(0).getDisplayName()).isEqualTo("jdoe");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void stubToken(String token) {
        KeycloakTokenResponse tokenResponse = new KeycloakTokenResponse();
        tokenResponse.setAccessToken(token);
        when(restTemplate.postForObject(
                contains("/protocol/openid-connect/token"),
                any(),
                eq(KeycloakTokenResponse.class)))
                .thenReturn(tokenResponse);
    }

    private void stubUserSearch(KeycloakUserRepresentation[] users) {
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(KeycloakUserRepresentation[].class)))
                .thenReturn(new ResponseEntity<>(users, HttpStatus.OK));
    }

    private KeycloakUserRepresentation keycloakUser(String id, String firstName, String lastName, String email) {
        KeycloakUserRepresentation u = new KeycloakUserRepresentation();
        u.setId(id);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        return u;
    }
}