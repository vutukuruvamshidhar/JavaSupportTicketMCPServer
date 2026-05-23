package com.supportticket.mcpserver;

import com.supportticket.mcpserver.resources.SupportTicketResource;
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
 * Unit tests for {@link com.supportticket.mcpserver.resources.SupportTicketResource}.
 *
 * <p>Verifies access control enforcement and that the template content read from the
 * classpath is returned correctly inside a {@link io.modelcontextprotocol.spec.McpSchema.ReadResourceResult}.
 * No MCP server or Keycloak instance is required.</p>
 */
@ExtendWith(MockitoExtension.class)
class SupportTicketResourceTest {

    @Mock
    private McpAccessService mcpAccessService;

    private SupportTicketResource resource;

    @BeforeEach
    void setUp() {
        resource = new SupportTicketResource(mcpAccessService);
    }

    // -----------------------------------------------------------------------
    // Access control
    // -----------------------------------------------------------------------

    @Test
    void getTicketCreationTemplate_throwsAccessDenied_whenAccessCheckFails() {
        doThrow(new AccessDeniedException("access_templates not granted"))
                .when(mcpAccessService).requireTemplateAccess();

        assertThatThrownBy(() -> resource.getTicketCreationTemplate())
                .isInstanceOf(AccessDeniedException.class);
    }

    // -----------------------------------------------------------------------
    // Happy path
    // -----------------------------------------------------------------------

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

    @Test
    void getTicketCreationTemplate_contentIsNotBlank() throws IOException {
        McpSchema.ReadResourceResult result = resource.getTicketCreationTemplate();

        McpSchema.TextResourceContents textContents =
                (McpSchema.TextResourceContents) result.contents().getFirst();
        assertThat(textContents.text()).isNotBlank();
    }
}