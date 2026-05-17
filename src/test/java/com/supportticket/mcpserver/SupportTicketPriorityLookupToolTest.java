package com.supportticket.mcpserver;

import com.supportticket.mcpserver.dto.CompanyPriority;
import com.supportticket.mcpserver.dto.PriorityResponse;
import com.supportticket.mcpserver.repository.SupportTicketRepo;
import com.supportticket.mcpserver.service.McpAccessService;
import com.supportticket.mcpserver.tools.SupportTicketPriorityLookupTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SupportTicketPriorityLookupTool}.
 *
 * <p>Verifies that the priority lookup tool correctly delegates to
 * {@link SupportTicketRepo} and returns a well-formed {@link PriorityResponse},
 * including the default fallback when no record is found.</p>
 */
@ExtendWith(MockitoExtension.class)
class SupportTicketPriorityLookupToolTest {

    @Mock
    private SupportTicketRepo supportTicketRepo;

    @Mock
    private McpAccessService mcpAccessService;

    private SupportTicketPriorityLookupTool tool;

    @BeforeEach
    void setUp() {
        tool = new SupportTicketPriorityLookupTool(supportTicketRepo, mcpAccessService);
    }

    /**
     * Verifies that when the repository returns a matching {@link CompanyPriority}
     * record, its priority value is returned in the response.
     */
    @Test
    void lookupPriority_returnsDbPriorityWhenCompanyFound() {
        CompanyPriority companyPriority = new CompanyPriority();
        companyPriority.setCompanyName("Acme Corp");
        companyPriority.setPriority(1);

        when(supportTicketRepo.findByCompanyName("Acme Corp")).thenReturn(Optional.of(companyPriority));

        PriorityResponse response = tool.lookupPriority("Acme Corp");

        assertThat(response).isNotNull();
        assertThat(response.getPriority()).isEqualTo(1);
    }

    /**
     * Verifies that when no record is found for the company name, the tool
     * returns the default priority of {@code 3} (MEDIUM).
     */
    @Test
    void lookupPriority_returnsDefaultPriorityWhenCompanyNotFound() {
        when(supportTicketRepo.findByCompanyName("Unknown Corp")).thenReturn(Optional.empty());

        PriorityResponse response = tool.lookupPriority("Unknown Corp");

        assertThat(response).isNotNull();
        assertThat(response.getPriority()).isEqualTo(3);
    }

    @Test
    void lookupPriority_throwsAccessDenied_whenAccessCheckFails() {
        doThrow(new AccessDeniedException("access_tools not granted"))
                .when(mcpAccessService).requireToolAccess();

        assertThatThrownBy(() -> tool.lookupPriority("Acme Corp"))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(supportTicketRepo);
    }
}
