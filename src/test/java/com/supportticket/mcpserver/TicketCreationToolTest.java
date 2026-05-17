package com.supportticket.mcpserver;

import com.supportticket.mcpserver.apiclient.TicketApiClient;
import com.supportticket.mcpserver.dto.Ticket;
import com.supportticket.mcpserver.exception.TicketCreationException;
import com.supportticket.mcpserver.service.McpAccessService;
import com.supportticket.mcpserver.tools.TicketCreationTool;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TicketCreationTool}.
 *
 * <p>Verifies the happy path, request field mapping, {@link FeignException}
 * handling, and generic exception handling without a live API.</p>
 */
@ExtendWith(MockitoExtension.class)
class TicketCreationToolTest {

    @Mock
    private TicketApiClient ticketApiClient;

    @Mock
    private McpAccessService mcpAccessService;

    private TicketCreationTool tool;

    @BeforeEach
    void setUp() {
        tool = new TicketCreationTool(ticketApiClient, mcpAccessService);
    }

    // -----------------------------------------------------------------------
    // Happy path
    // -----------------------------------------------------------------------

    /**
     * Verifies that a successful API response is returned with
     * {@code status = "success"} and a non-null {@code creationDate}.
     */
    @Test
    void createTicket_returnsTicketWithStatusSuccessAndCreationDate() {
        Ticket apiResponse = new Ticket(
                "Jane Doe", "Printer not working", "Acme Corp",
                2, "Bob Support", "bob@support.com", null, null
        );
        when(ticketApiClient.createTicket(any(Ticket.class))).thenReturn(apiResponse);

        Ticket result = tool.createTicket(
                "Jane Doe", "Printer not working", "Acme Corp",
                2, "Bob Support", "bob@support.com"
        );

        assertThat(result.getStatus()).isEqualTo("success");
        assertThat(result.getCreationDate()).isNotNull();
    }

    /**
     * Verifies that all six input parameters are correctly mapped to the
     * request body sent to the Feign client.
     */
    @Test
    void createTicket_sendsAllFieldsInRequestBody() {
        Ticket apiResponse = new Ticket(
                "Jane Doe", "Printer not working", "Acme Corp",
                2, "Bob Support", "bob@support.com", null, null
        );
        when(ticketApiClient.createTicket(any(Ticket.class))).thenReturn(apiResponse);

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);

        tool.createTicket("Jane Doe", "Printer not working", "Acme Corp",
                2, "Bob Support", "bob@support.com");

        verify(ticketApiClient).createTicket(captor.capture());
        Ticket sent = captor.getValue();

        assertThat(sent.getRequestorName()).isEqualTo("Jane Doe");
        assertThat(sent.getTicketDescription()).isEqualTo("Printer not working");
        assertThat(sent.getCompanyName()).isEqualTo("Acme Corp");
        assertThat(sent.getPriority()).isEqualTo(2);
        assertThat(sent.getAssigneeName()).isEqualTo("Bob Support");
        assertThat(sent.getAssigneeEmail()).isEqualTo("bob@support.com");
    }

    /**
     * Verifies that the request body sent to the API does not include
     * {@code creationDate} or {@code status} (those are response-only fields).
     */
    @Test
    void createTicket_doesNotSetCreationDateOrStatusOnRequest() {
        Ticket apiResponse = new Ticket(
                "Jane Doe", "Printer not working", "Acme Corp",
                2, "Bob Support", "bob@support.com", null, null
        );
        when(ticketApiClient.createTicket(any(Ticket.class))).thenReturn(apiResponse);

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        tool.createTicket("Jane Doe", "Printer not working", "Acme Corp",
                2, "Bob Support", "bob@support.com");

        verify(ticketApiClient).createTicket(captor.capture());
        assertThat(captor.getValue().getCreationDate()).isNull();
        assertThat(captor.getValue().getStatus()).isNull();
    }

    // -----------------------------------------------------------------------
    // FeignException handling
    // -----------------------------------------------------------------------

    /**
     * Verifies that a {@link FeignException} from the Feign client is wrapped
     * in a {@link TicketCreationException} with the HTTP status in the message.
     */
    @Test
    void createTicket_throwsTicketCreationExceptionOnFeignException() {
        Request dummyRequest = Request.create(
                Request.HttpMethod.POST, "http://localhost/ticket",
                Map.of(), null, StandardCharsets.UTF_8, null
        );
        FeignException feignException = FeignException.errorStatus(
                "createTicket",
                feign.Response.builder()
                        .status(503)
                        .reason("Service Unavailable")
                        .request(dummyRequest)
                        .headers(Map.of())
                        .build()
        );
        when(ticketApiClient.createTicket(any(Ticket.class))).thenThrow(feignException);

        assertThatThrownBy(() -> tool.createTicket(
                "Jane Doe", "Printer not working", "Acme Corp",
                2, "Bob Support", "bob@support.com"))
                .isInstanceOf(TicketCreationException.class)
                .hasMessageContaining("503")
                .hasCause(feignException);
    }

    // -----------------------------------------------------------------------
    // Access control
    // -----------------------------------------------------------------------

    @Test
    void createTicket_throwsAccessDenied_whenAccessCheckFails() {
        doThrow(new AccessDeniedException("access_tools not granted"))
                .when(mcpAccessService).requireToolAccess();

        assertThatThrownBy(() -> tool.createTicket(
                "Jane Doe", "Printer not working", "Acme Corp",
                2, "Bob Support", "bob@support.com"))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(ticketApiClient);
    }

    // -----------------------------------------------------------------------
    // Generic exception handling
    // -----------------------------------------------------------------------

    /**
     * Verifies that any non-Feign runtime exception is also wrapped in a
     * {@link TicketCreationException}.
     */
    @Test
    void createTicket_throwsTicketCreationExceptionOnUnexpectedError() {
        RuntimeException unexpected = new RuntimeException("connection reset");
        when(ticketApiClient.createTicket(any(Ticket.class))).thenThrow(unexpected);

        assertThatThrownBy(() -> tool.createTicket(
                "Jane Doe", "Printer not working", "Acme Corp",
                2, "Bob Support", "bob@support.com"))
                .isInstanceOf(TicketCreationException.class)
                .hasMessageContaining("unexpected error")
                .hasCause(unexpected);
    }
}
