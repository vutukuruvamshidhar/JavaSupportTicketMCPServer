package com.supportticket.mcpserver.resources;

import com.supportticket.mcpserver.service.McpAccessService;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * MCP resource provider that exposes the ticket creation template.
 *
 * <p>Registers a static, read-only resource with the MCP server so that AI
 * clients can retrieve the structured template used when generating new support
 * tickets. The template file is resolved from the classpath at invocation time,
 * allowing its content to be updated without recompiling the application.</p>
 */
@Component
public class SupportTicketResource {

    private static final Logger log = LoggerFactory.getLogger(SupportTicketResource.class);

    private final McpAccessService mcpAccessService;

    /**
     * Creates the resource provider with the given access-control service.
     *
     * @param mcpAccessService the service that enforces JWT-based access checks
     */
    public SupportTicketResource(McpAccessService mcpAccessService) {
        this.mcpAccessService = mcpAccessService;
    }

    /**
     * Returns the ticket creation template as an MCP resource.
     *
     * <p>Reads {@code ticket_creation_template.txt} from the classpath and wraps
     * its content in a {@link McpSchema.ReadResourceResult} containing a single
     * {@link McpSchema.TextResourceContents} entry with MIME type
     * {@code text/plain}.</p>
     *
     * @return a {@link McpSchema.ReadResourceResult} whose single content entry
     *         holds the raw template text
     * @throws IOException if the template file cannot be read from the classpath
     */
    @McpResource(
            name = "ticket-creation-template",
            title = "Ticket Creation Template",
            uri = "resource:///ticket-creation-template",
            description = "Template used to generate structured support ticket content",
            mimeType = "text/plain"
    )
    public McpSchema.ReadResourceResult getTicketCreationTemplate() throws IOException {
        log.info("*********Resources called***********");
        mcpAccessService.requireTemplateAccess();
        String content = new ClassPathResource("ticket_creation_template.txt")
                .getContentAsString(StandardCharsets.UTF_8);

        return new McpSchema.ReadResourceResult(
                List.of(new McpSchema.TextResourceContents(
                        "resource:///ticket-creation-template",
                        "text/plain",
                        content
                ))
        );
    }
}
