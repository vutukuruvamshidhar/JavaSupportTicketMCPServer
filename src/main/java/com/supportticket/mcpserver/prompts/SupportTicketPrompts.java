package com.supportticket.mcpserver.prompts;

import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * MCP prompt provider for support ticket operations.
 *
 * <p>Exposes prompts that guide an AI assistant through support ticket workflows.
 * Each prompt method is discovered and registered automatically by the MCP
 * annotation scanner at application startup.</p>
 */
@Component
public class SupportTicketPrompts {

    private static final Logger log = LoggerFactory.getLogger(SupportTicketPrompts.class);

    /**
     * Returns the prompt that instructs an AI assistant to create and assign a
     * support ticket using the available MCP tools.
     *
     * <p>The prompt text is loaded at invocation time from
     * {@code ticket_creation_prompt.txt} on the classpath, keeping the
     * instruction content decoupled from the source code.</p>
     *
     * @return a {@link McpSchema.GetPromptResult} containing a single {@code USER}
     *         message whose text is the full contents of the prompt file
     * @throws IOException if the prompt file cannot be read from the classpath
     */
    @McpPrompt(
            name = "create-and-assign-ticket",
            title = "Create and Assign Ticket",
            description = "Guides the AI to create a new support ticket and assign it to a team member"
    )
    public McpSchema.GetPromptResult createAndAssignTicketPrompt() throws IOException {
        String userMessage = new ClassPathResource("ticket_creation_prompt.txt")
                .getContentAsString(StandardCharsets.UTF_8);

        log.info("inside createAndAssignTicketPrompt");
        return new McpSchema.GetPromptResult(
                "Create and assign a support ticket",
                List.of(new McpSchema.PromptMessage(
                        McpSchema.Role.USER,
                        new McpSchema.TextContent(userMessage)
                ))
        );
    }
}