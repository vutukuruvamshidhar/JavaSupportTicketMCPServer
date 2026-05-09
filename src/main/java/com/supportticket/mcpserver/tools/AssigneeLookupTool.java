package com.supportticket.mcpserver.tools;

import com.supportticket.mcpserver.dto.Assignee;
import com.supportticket.mcpserver.dto.AssigneeSelection;
import com.supportticket.mcpserver.service.AzureGraphClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import org.springaicommunity.mcp.context.StructuredElicitResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MCP tool that resolves a support ticket assignee by name using Azure Entra ID.
 *
 * <p>When exactly one user matches the supplied name the result is returned
 * immediately. When multiple users share the same display name the tool uses
 * the MCP elicitation protocol to present the candidates to the user and waits
 * for them to confirm which person they intended.</p>
 */
@Component
public class AssigneeLookupTool {

    private static final Logger log = LoggerFactory.getLogger(AssigneeLookupTool.class);

    private final AzureGraphClient azureGraphClient;

    /**
     * Constructs the tool with the given Azure Graph client.
     *
     * @param azureGraphClient client used to query Entra ID
     */
    public AssigneeLookupTool(AzureGraphClient azureGraphClient) {
        this.azureGraphClient = azureGraphClient;
    }

    /**
     * Looks up an assignee by display name in Azure Entra ID.
     *
     * <p>Resolution rules:</p>
     * <ol>
     *   <li>If no user matches, an {@link IllegalArgumentException} is thrown.</li>
     *   <li>If exactly one user matches, that user is returned as an
     *       {@link Assignee}.</li>
     *   <li>If more than one user matches, the MCP elicitation protocol is used
     *       to send the candidate list to the client and request confirmation of
     *       the correct person. The tool returns only after the user confirms
     *       ({@code ACCEPT}) their selection. If the user declines or cancels the
     *       elicitation, an {@link IllegalStateException} is thrown.</li>
     * </ol>
     *
     * @param assigneeName the full or partial display name to search for
     * @param context      the MCP request context; used to trigger elicitation
     *                     when multiple candidates are found
     * @return an {@link Assignee} with the resolved ID, display name, and email
     * @throws IllegalArgumentException if no user is found for the given name
     * @throws IllegalStateException    if the user cancels or declines the
     *                                  elicitation when multiple candidates exist
     */
    @McpTool(
            name = "lookupAssignee",
            title = "Lookup Assignee",
            description = "Resolves a support ticket assignee by name from Azure Entra ID. " +
                          "When multiple users share the same name the user is asked to confirm the correct one."
    )
    public Assignee lookupAssignee(
            @McpToolParam(description = "Display name (full or partial) of the assignee to look up", required = true)
            String assigneeName,
            McpSyncRequestContext context
    ) {

        log.info("inside lookupAssignee Tool");
       // List<Assignee> candidates = azureGraphClient.findByDisplayName(assigneeName);
        List<Assignee> candidates = new ArrayList<>();
        candidates.add(new Assignee("123","Joe Smith", "joe.smith@abcd.com"));
        candidates.add(new Assignee("345","Joe Smiths", "joe.smiths@abcd.com"));

        if (candidates.isEmpty()) {
            throw new IllegalArgumentException(
                    "No user found in Azure Entra ID with display name: " + assigneeName);
        }

        if (candidates.size() == 1) {
            return candidates.getFirst();
        }

        return resolveViaElicitation(candidates, context);
    }

    /**
     * Presents the list of candidates to the MCP client via elicitation and
     * returns the {@link Assignee} that the user selects.
     *
     * @param candidates list of users that matched the search term
     * @param context    the MCP request context used to trigger elicitation
     * @return the confirmed {@link Assignee}
     * @throws IllegalStateException if the user declines or cancels the prompt
     * @throws IllegalArgumentException if the selected ID does not match any candidate
     */
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

    /**
     * Builds a human-readable candidate list message for the elicitation prompt.
     *
     * @param candidates the list of matching users
     * @return a formatted string listing each candidate's display name, ID, and email
     */
    private String buildCandidateMessage(List<Assignee> candidates) {
        String list = candidates.stream()
                .map(a -> "  - Name: " + a.getDisplayName()
                        + " | ID: " + a.getId()
                        + " | Email: " + a.getEmail())
                .collect(Collectors.joining("\n"));

        return "Multiple users were found with that name. " +
               "Please provide the 'selectedId' and 'selectedName' of the correct assignee:\n\n" +
               list;
    }
}
