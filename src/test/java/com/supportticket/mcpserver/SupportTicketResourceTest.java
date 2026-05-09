package com.supportticket.mcpserver;

import com.supportticket.mcpserver.resources.SupportTicketResource;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link SupportTicketResource}.
 *
 * <p>Verifies that the MCP resource method correctly reads the template file
 * from the classpath and returns a well-formed {@link McpSchema.ReadResourceResult}
 * with the expected URI, MIME type, and content.</p>
 */
class SupportTicketResourceTest {

    private SupportTicketResource resource;

    /**
     * Creates a fresh {@link SupportTicketResource} instance before each test.
     */
    @BeforeEach
    void setUp() {
        resource = new SupportTicketResource();
    }

    /**
     * Verifies that the returned result is non-null, contains exactly one
     * {@link McpSchema.TextResourceContents} entry, and that its URI, MIME type,
     * and text match the expected values from {@code ticket_creation_template.txt}.
     */
    @Test
    void getTicketCreationTemplate_returnsResultWithFileContents() throws IOException {
        McpSchema.ReadResourceResult result = resource.getTicketCreationTemplate();

        String expectedContent = new ClassPathResource("ticket_creation_template.txt")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(result).isNotNull();

        List<McpSchema.ResourceContents> contents = result.contents();
        assertThat(contents).hasSize(1);

        McpSchema.TextResourceContents textContents = (McpSchema.TextResourceContents) contents.getFirst();
        assertThat(textContents.uri()).isEqualTo("resource:///ticket-creation-template");
        assertThat(textContents.mimeType()).isEqualTo("text/plain");
        assertThat(textContents.text()).isEqualTo(expectedContent);
    }

    /**
     * Sanity check that the loaded template text is not blank.
     */
    @Test
    void getTicketCreationTemplate_contentIsNotBlank() throws IOException {
        McpSchema.ReadResourceResult result = resource.getTicketCreationTemplate();

        McpSchema.TextResourceContents textContents = (McpSchema.TextResourceContents) result.contents().getFirst();
        assertThat(textContents.text()).isNotBlank();
    }
}
