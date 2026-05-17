package com.supportticket.mcpserver;

import com.supportticket.mcpserver.dto.Assignee;
import com.supportticket.mcpserver.dto.AssigneeSelection;
import com.supportticket.mcpserver.service.KeycloakUserService;
import com.supportticket.mcpserver.service.McpAccessService;
import com.supportticket.mcpserver.tools.AssigneeLookupTool;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import org.springaicommunity.mcp.context.StructuredElicitResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AssigneeLookupTool}.
 *
 * <p>Covers single-match, multi-match (elicitation accept / decline / cancel),
 * and no-match scenarios. {@link KeycloakUserService} is mocked so no real
 * Keycloak server is required.</p>
 */
@ExtendWith(MockitoExtension.class)
class AssigneeLookupToolTest {

    @Mock
    private KeycloakUserService keycloakUserService;

    @Mock
    private McpAccessService mcpAccessService;

    @Mock
    private McpSyncRequestContext context;

    private AssigneeLookupTool tool;

    @BeforeEach
    void setUp() {
        tool = new AssigneeLookupTool(keycloakUserService, mcpAccessService);
    }

    // -----------------------------------------------------------------------
    // Access control
    // -----------------------------------------------------------------------

    @Test
    void lookupAssignee_throwsAccessDenied_whenAccessCheckFails() {
        doThrow(new AccessDeniedException("access_tools not granted"))
                .when(mcpAccessService).requireToolAccess();

        assertThatThrownBy(() -> tool.lookupAssignee("Joe", context))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(keycloakUserService, context);
    }

    // -----------------------------------------------------------------------
    // Single match
    // -----------------------------------------------------------------------

    @Test
    void lookupAssignee_returnsSingleMatchWithoutElicitation() {
        Assignee alice = new Assignee("id-001", "Alice Smith", "alice@example.com");
        when(keycloakUserService.findUsersByFirstName("Alice")).thenReturn(List.of(alice));

        Assignee result = tool.lookupAssignee("Alice", context);

        assertThat(result.getId()).isEqualTo("id-001");
        assertThat(result.getDisplayName()).isEqualTo("Alice Smith");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        verifyNoInteractions(context);
    }

    // -----------------------------------------------------------------------
    // No match
    // -----------------------------------------------------------------------

    @Test
    void lookupAssignee_throwsWhenNoMatchFound() {
        when(keycloakUserService.findUsersByFirstName("Unknown")).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> tool.lookupAssignee("Unknown", context))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No user found");
    }

    // -----------------------------------------------------------------------
    // Multiple matches – elicitation accepted
    // -----------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void lookupAssignee_returnsSelectedAssigneeOnElicitationAccept() {
        Assignee joe1 = new Assignee("123", "Joe Smith", "joe.smith@abcd.com");
        Assignee joe2 = new Assignee("345", "Joe Smiths", "joe.smiths@abcd.com");
        when(keycloakUserService.findUsersByFirstName("Joe")).thenReturn(List.of(joe1, joe2));

        AssigneeSelection selection = new AssigneeSelection();
        selection.setSelectedId("345");
        selection.setSelectedName("Joe Smiths");

        StructuredElicitResult<AssigneeSelection> elicitResult = new StructuredElicitResult<>(
                McpSchema.ElicitResult.Action.ACCEPT, selection, Map.of());

        when(context.elicit(any(Consumer.class), eq(AssigneeSelection.class)))
                .thenReturn(elicitResult);

        Assignee result = tool.lookupAssignee("Joe", context);

        assertThat(result.getId()).isEqualTo("345");
        assertThat(result.getEmail()).isEqualTo("joe.smiths@abcd.com");
    }

    @Test
    @SuppressWarnings("unchecked")
    void lookupAssignee_elicitationMessageContainsAllCandidates() {
        Assignee joe1 = new Assignee("123", "Joe Smith", "joe.smith@abcd.com");
        Assignee joe2 = new Assignee("345", "Joe Smiths", "joe.smiths@abcd.com");
        when(keycloakUserService.findUsersByFirstName("Joe")).thenReturn(List.of(joe1, joe2));

        AssigneeSelection selection = new AssigneeSelection();
        selection.setSelectedId("123");
        selection.setSelectedName("Joe Smith");

        StructuredElicitResult<AssigneeSelection> elicitResult = new StructuredElicitResult<>(
                McpSchema.ElicitResult.Action.ACCEPT, selection, Map.of());

        ArgumentCaptor<Consumer<org.springaicommunity.mcp.context.McpRequestContextTypes.ElicitationSpec>>
                specCaptor = ArgumentCaptor.forClass(Consumer.class);

        when(context.elicit(specCaptor.capture(), eq(AssigneeSelection.class)))
                .thenReturn(elicitResult);

        tool.lookupAssignee("Joe", context);

        verify(context).elicit(any(Consumer.class), eq(AssigneeSelection.class));
    }

    // -----------------------------------------------------------------------
    // Multiple matches – elicitation declined or cancelled
    // -----------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void lookupAssignee_throwsWhenElicitationDeclined() {
        Assignee joe1 = new Assignee("123", "Joe Smith", "joe.smith@abcd.com");
        Assignee joe2 = new Assignee("345", "Joe Smiths", "joe.smiths@abcd.com");
        when(keycloakUserService.findUsersByFirstName("Joe")).thenReturn(List.of(joe1, joe2));

        StructuredElicitResult<AssigneeSelection> elicitResult = new StructuredElicitResult<>(
                McpSchema.ElicitResult.Action.DECLINE, null, Map.of());

        when(context.elicit(any(Consumer.class), eq(AssigneeSelection.class)))
                .thenReturn(elicitResult);

        assertThatThrownBy(() -> tool.lookupAssignee("Joe", context))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("decline");
    }

    @Test
    @SuppressWarnings("unchecked")
    void lookupAssignee_throwsWhenElicitationCancelled() {
        Assignee joe1 = new Assignee("123", "Joe Smith", "joe.smith@abcd.com");
        Assignee joe2 = new Assignee("345", "Joe Smiths", "joe.smiths@abcd.com");
        when(keycloakUserService.findUsersByFirstName("Joe")).thenReturn(List.of(joe1, joe2));

        StructuredElicitResult<AssigneeSelection> elicitResult = new StructuredElicitResult<>(
                McpSchema.ElicitResult.Action.CANCEL, null, Map.of());

        when(context.elicit(any(Consumer.class), eq(AssigneeSelection.class)))
                .thenReturn(elicitResult);

        assertThatThrownBy(() -> tool.lookupAssignee("Joe", context))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cancel");
    }
}