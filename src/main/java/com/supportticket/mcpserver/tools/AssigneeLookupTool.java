package com.supportticket.mcpserver.tools;

import com.supportticket.mcpserver.dto.Assignee;
import com.supportticket.mcpserver.dto.AssigneeSelection;
import com.supportticket.mcpserver.service.KeycloakUserService;
import com.supportticket.mcpserver.service.McpAccessService;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import org.springaicommunity.mcp.context.StructuredElicitResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MCP tool that resolves a support ticket assignee by first name via Keycloak.
 *
 * <p>When exactly one user matches the supplied first name the result is returned
 * immediately. When multiple users share the same first name the tool uses the MCP
 * elicitation protocol to present the candidates and waits for the user to pick one.</p>
 */
@Component
public class AssigneeLookupTool {

    private static final Logger log = LoggerFactory.getLogger(AssigneeLookupTool.class);

    private final KeycloakUserService keycloakUserService;
    private final McpAccessService mcpAccessService;

    /**
     * Creates the tool with the given user service and access-control service.
     *
     * @param keycloakUserService service used to query Keycloak for matching users
     * @param mcpAccessService    service that enforces JWT-based access checks
     */
    public AssigneeLookupTool(KeycloakUserService keycloakUserService,
                               McpAccessService mcpAccessService) {
        this.keycloakUserService = keycloakUserService;
        this.mcpAccessService = mcpAccessService;
    }

    /**
     * Resolves a ticket assignee by first name.
     *
     * <p>Queries Keycloak for users matching the supplied first name. If exactly
     * one user is found it is returned immediately. If multiple candidates exist
     * the tool uses the MCP elicitation protocol to ask the user to pick the
     * correct one. If no users are found an {@link IllegalArgumentException} is
     * thrown.</p>
     *
     * @param assigneeName the first name of the assignee to look up
     * @param context      the MCP request context, used to trigger elicitation
     *                     when multiple candidates are found
     * @return the resolved {@link Assignee}
     * @throws IllegalArgumentException if no user with the given first name exists
     * @throws IllegalStateException    if the elicitation is declined or cancelled
     */
    @McpTool(
            name = "lookupAssignee",
            title = "Lookup Assignee",
            description = "Resolves a support ticket assignee by first name via Keycloak. " +
                          "When multiple users share the same first name the user is asked to confirm the correct one."
    )
    public Assignee lookupAssignee(
            @McpToolParam(description = "First name of the assignee to look up", required = true)
            String assigneeName,
            McpSyncRequestContext context
    ) {
        log.info("Looking up assignee by first name: {}", assigneeName);
        mcpAccessService.requireToolAccess();

        List<Assignee> candidates = keycloakUserService.findUsersByFirstName(assigneeName);

        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No user found with first name: " + assigneeName);
        }

        if (candidates.size() == 1) {
            return candidates.getFirst();
        }

        return resolveViaElicitation(candidates, context);
    }

    private Assignee resolveViaElicitation(List<Assignee> candidates, McpSyncRequestContext context) {
        String message = buildCandidateMessage(candidates);

        StructuredElicitResult<AssigneeSelection> result = context.elicit(
                spec -> spec.message(message),
                AssigneeSelection.class
        );

        if (result.action() != McpSchema.ElicitResult.Action.ACCEPT) {
            throw new IllegalStateException(
                    "Assignee selection was " + result.action().name().toLowerCase() +
                    " by the user. Please retry with a more specific name.");
        }

        AssigneeSelection selection = result.structuredContent();
        return candidates.stream()
                .filter(a -> a.getId().equals(selection.getSelectedId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "The selected ID '" + selection.getSelectedId() + "' does not match any candidate."));
    }

    private String buildCandidateMessage(List<Assignee> candidates) {
        String list = candidates.stream()
                .map(a -> "  - Name: " + a.getDisplayName()
                        + " | ID: " + a.getId()
                        + " | Email: " + a.getEmail())
                .collect(Collectors.joining("\n"));

        return "Multiple users were found with that first name. " +
               "Please provide the 'selectedId' and 'selectedName' of the correct assignee:\n\n" +
               list;
    }
}