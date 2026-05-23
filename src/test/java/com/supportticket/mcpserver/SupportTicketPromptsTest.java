package com.supportticket.mcpserver;

import com.supportticket.mcpserver.prompts.SupportTicketPrompts;
import com.supportticket.mcpserver.service.McpAccessService;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

/**
 * Unit tests for {@link com.supportticket.mcpserver.prompts.SupportTicketPrompts}.
 *
 * <p>Verifies access control enforcement and that the prompt content read from the
 * classpath is wrapped correctly inside a {@link io.modelcontextprotocol.spec.McpSchema.GetPromptResult}.
 * No MCP server or Keycloak instance is required.</p>
 */
@ExtendWith(MockitoExtension.class)
class SupportTicketPromptsTest {

    @Mock
    private McpAccessService mcpAccessService;

    private SupportTicketPrompts prompts;

    @BeforeEach
    void setUp() {
        prompts = new SupportTicketPrompts(mcpAccessService);
    }

    // -----------------------------------------------------------------------
    // Access control
    // -----------------------------------------------------------------------

    @Test
    void createAndAssignTicketPrompt_throwsAccessDenied_whenAccessCheckFails() {
        doThrow(new AccessDeniedException("access_prompts not granted"))
                .when(mcpAccessService).requirePromptAccess();

        assertThatThrownBy(() -> prompts.createAndAssignTicketPrompt())
                .isInstanceOf(AccessDeniedException.class);
    }

    // -----------------------------------------------------------------------
    // Happy path
    // -----------------------------------------------------------------------

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

    @Test
    void createAndAssignTicketPrompt_promptContentIsNotBlank() throws IOException {
        McpSchema.GetPromptResult result = prompts.createAndAssignTicketPrompt();

        String text = ((McpSchema.TextContent) result.messages().getFirst().content()).text();
        assertThat(text).isNotBlank();
    }
}