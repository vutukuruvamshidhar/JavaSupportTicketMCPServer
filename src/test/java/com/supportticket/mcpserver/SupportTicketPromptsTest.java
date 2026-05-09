package com.supportticket.mcpserver;

import com.supportticket.mcpserver.prompts.SupportTicketPrompts;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link SupportTicketPrompts}.
 *
 * <p>Verifies that the MCP prompt method correctly reads the prompt file from
 * the classpath and returns a well-formed {@link McpSchema.GetPromptResult}.</p>
 */
class SupportTicketPromptsTest {

    private SupportTicketPrompts prompts;

    /**
     * Creates a fresh {@link SupportTicketPrompts} instance before each test.
     */
    @BeforeEach
    void setUp() {
        prompts = new SupportTicketPrompts();
    }

    /**
     * Verifies that the returned result is non-null, carries the expected
     * description, contains exactly one {@code USER} message, and that the
     * message text exactly matches the contents of {@code ticket_creation_prompt.txt}.
     */
    @Test
    void createAndAssignTicketPrompt_returnsPromptResultWithFileContents() throws IOException {
        McpSchema.GetPromptResult result = prompts.createAndAssignTicketPrompt();

        String expectedContent = new ClassPathResource("ticket_creation_prompt.txt")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(result).isNotNull();
        assertThat(result.description()).isEqualTo("Create and assign a support ticket");

        List<McpSchema.PromptMessage> messages = result.messages();
        assertThat(messages).hasSize(1);

        McpSchema.PromptMessage message = messages.getFirst();
        assertThat(message.role()).isEqualTo(McpSchema.Role.USER);
        assertThat(((McpSchema.TextContent) message.content()).text()).isEqualTo(expectedContent);
    }

    /**
     * Sanity check that the loaded prompt text is not blank.
     */
    @Test
    void createAndAssignTicketPrompt_promptContentIsNotBlank() throws IOException {
        McpSchema.GetPromptResult result = prompts.createAndAssignTicketPrompt();

        String text = ((McpSchema.TextContent) result.messages().getFirst().content()).text();
        assertThat(text).isNotBlank();
    }

}
