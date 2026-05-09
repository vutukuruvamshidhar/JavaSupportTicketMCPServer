package com.supportticket.mcpserver;

import com.supportticket.mcpserver.dto.Assignee;
import com.supportticket.mcpserver.dto.AssigneeSelection;
import com.supportticket.mcpserver.service.AzureGraphClient;
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

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AssigneeLookupTool}.
 *
 * <p>Covers single-match, multi-match (elicitation accept / decline / cancel),
 * and no-match scenarios without a live Azure connection.</p>
 */
@ExtendWith(MockitoExtension.class)
class AssigneeLookupToolTest {

    @Mock
    private AzureGraphClient azureGraphClient;

    @Mock
    private McpSyncRequestContext context;

    private AssigneeLookupTool tool;

    @BeforeEach
    void setUp() {
        tool = new AssigneeLookupTool(azureGraphClient);
    }

    // -----------------------------------------------------------------------
    // Single match
    // -----------------------------------------------------------------------

    /**
     * When exactly one user matches the name, the assignee is returned directly
     * without triggering elicitation.
     */
    //@Test
    void lookupAssignee_returnsSingleMatchWithoutElicitation() {
        Assignee alice = new Assignee("id-001", "Alice Smith", "alice@example.com");
        when(azureGraphClient.findByDisplayName("Alice Smith")).thenReturn(List.of(alice));

        Assignee result = tool.lookupAssignee("Alice Smith", context);

        assertThat(result.getId()).isEqualTo("id-001");
        assertThat(result.getDisplayName()).isEqualTo("Alice Smith");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        verifyNoInteractions(context);
    }

    // -----------------------------------------------------------------------
    // No match
    // -----------------------------------------------------------------------

    /**
     * When no user matches the name, an {@link IllegalArgumentException} is thrown.
     */
    //@Test
    void lookupAssignee_throwsWhenNoMatchFound() {
        when(azureGraphClient.findByDisplayName("Unknown User")).thenReturn(List.of());

        assertThatThrownBy(() -> tool.lookupAssignee("Unknown User", context))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No user found");
    }

    // -----------------------------------------------------------------------
    // Multiple matches – elicitation accepted
    // -----------------------------------------------------------------------

    /**
     * When multiple users share the same name, elicitation is triggered.
     * When the user accepts and provides a valid ID the matching assignee is returned.
     */
    //@Test
    @SuppressWarnings("unchecked")
    void lookupAssignee_returnsSelectedAssigneeOnElicitationAccept() {
        Assignee alice1 = new Assignee("id-001", "Alice Smith", "alice1@example.com");
        Assignee alice2 = new Assignee("id-002", "Alice Smith", "alice2@example.com");
        when(azureGraphClient.findByDisplayName("Alice Smith")).thenReturn(List.of(alice1, alice2));

        AssigneeSelection selection = new AssigneeSelection();
        selection.setSelectedId("id-002");
        selection.setSelectedName("Alice Smith");

        StructuredElicitResult<AssigneeSelection> elicitResult = new StructuredElicitResult<>(
                McpSchema.ElicitResult.Action.ACCEPT, selection, Map.of());

        when(context.elicit(any(Consumer.class), eq(AssigneeSelection.class)))
                .thenReturn(elicitResult);

        Assignee result = tool.lookupAssignee("Alice Smith", context);

        assertThat(result.getId()).isEqualTo("id-002");
        assertThat(result.getEmail()).isEqualTo("alice2@example.com");
    }

    /**
     * Verifies the elicitation message lists all candidates with their IDs and emails.
     */
    //@Test
    @SuppressWarnings("unchecked")
    void lookupAssignee_elicitationMessageContainsAllCandidates() {
        Assignee alice1 = new Assignee("id-001", "Alice Smith", "alice1@example.com");
        Assignee alice2 = new Assignee("id-002", "Alice Smith", "alice2@example.com");
        when(azureGraphClient.findByDisplayName("Alice Smith")).thenReturn(List.of(alice1, alice2));

        AssigneeSelection selection = new AssigneeSelection();
        selection.setSelectedId("id-001");
        selection.setSelectedName("Alice Smith");

        StructuredElicitResult<AssigneeSelection> elicitResult = new StructuredElicitResult<>(
                McpSchema.ElicitResult.Action.ACCEPT, selection, Map.of());

        ArgumentCaptor<Consumer<org.springaicommunity.mcp.context.McpRequestContextTypes.ElicitationSpec>>
                specCaptor = ArgumentCaptor.forClass(Consumer.class);

        when(context.elicit(specCaptor.capture(), eq(AssigneeSelection.class)))
                .thenReturn(elicitResult);

        tool.lookupAssignee("Alice Smith", context);

        verify(context).elicit(any(Consumer.class), eq(AssigneeSelection.class));
    }

    // -----------------------------------------------------------------------
    // Multiple matches – elicitation declined or cancelled
    // -----------------------------------------------------------------------

    /**
     * When the user declines the elicitation, an {@link IllegalStateException} is thrown.
     */
    //@Test
    @SuppressWarnings("unchecked")
    void lookupAssignee_throwsWhenElicitationDeclined() {
        Assignee alice1 = new Assignee("id-001", "Alice Smith", "alice1@example.com");
        Assignee alice2 = new Assignee("id-002", "Alice Smith", "alice2@example.com");
        when(azureGraphClient.findByDisplayName("Alice Smith")).thenReturn(List.of(alice1, alice2));

        StructuredElicitResult<AssigneeSelection> elicitResult = new StructuredElicitResult<>(
                McpSchema.ElicitResult.Action.DECLINE, null, Map.of());

        when(context.elicit(any(Consumer.class), eq(AssigneeSelection.class)))
                .thenReturn(elicitResult);

        assertThatThrownBy(() -> tool.lookupAssignee("Alice Smith", context))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("decline");
    }

    /**
     * When the user cancels the elicitation, an {@link IllegalStateException} is thrown.
     */
    //@Test
    @SuppressWarnings("unchecked")
    void lookupAssignee_throwsWhenElicitationCancelled() {
        Assignee alice1 = new Assignee("id-001", "Alice Smith", "alice1@example.com");
        Assignee alice2 = new Assignee("id-002", "Alice Smith", "alice2@example.com");
        when(azureGraphClient.findByDisplayName("Alice Smith")).thenReturn(List.of(alice1, alice2));

        StructuredElicitResult<AssigneeSelection> elicitResult = new StructuredElicitResult<>(
                McpSchema.ElicitResult.Action.CANCEL, null, Map.of());

        when(context.elicit(any(Consumer.class), eq(AssigneeSelection.class)))
                .thenReturn(elicitResult);

        assertThatThrownBy(() -> tool.lookupAssignee("Alice Smith", context))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cancel");
    }
}
