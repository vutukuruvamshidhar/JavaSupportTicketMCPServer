package com.supportticket.mcpserver.tools;

import com.supportticket.mcpserver.apiclient.TicketApiClient;
import com.supportticket.mcpserver.dto.Ticket;
import com.supportticket.mcpserver.exception.TicketCreationException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * MCP tool that creates a new support ticket via the ticketing REST API.
 *
 * <p>Accepts all ticket fields as individual parameters, builds a {@link Ticket}
 * request body, and delegates to {@link TicketApiClient}. On success it enriches
 * the returned object with a {@code creationDate} timestamp and a {@code status}
 * of {@code "success"} before returning it to the MCP client.</p>
 *
 * <p>Any Feign or connectivity error is wrapped in a {@link TicketCreationException}
 * with a message that is safe to surface to the AI assistant.</p>
 */
@Component
public class TicketCreationTool {

    private static final Logger log = LoggerFactory.getLogger(TicketCreationTool.class);

    private final TicketApiClient ticketApiClient;

    /**
     * Constructs the tool with the given Feign client.
     *
     * @param ticketApiClient client used to call the ticketing REST API
     */
    public TicketCreationTool(TicketApiClient ticketApiClient) {
        this.ticketApiClient = ticketApiClient;
    }

    /**
     * Creates a new support ticket in the ticketing system.
     *
     * <p>The ticket is sent to {@code POST /ticket} via the Feign client. If the
     * call succeeds, the returned {@link Ticket} is stamped with the current
     * timestamp as {@code creationDate} and {@code status = "success"}.</p>
     *
     * @param requestorName     full name of the person raising the ticket
     * @param ticketDescription detailed description of the issue
     * @param companyName       company associated with the ticket
     * @param priority          numeric priority (1 = CRITICAL, 2 = HIGH, 3 = MEDIUM, 4 = LOW)
     * @param assigneeName      full display name of the support agent to assign
     * @param assigneeEmail     email address of the support agent to assign
     * @return the created {@link Ticket} with {@code creationDate} and
     *         {@code status = "success"} populated
     * @throws TicketCreationException if the API call fails for any reason
     */
    @McpTool(
            name = "createTicket",
            title = "Create Ticket",
            description = "Creates a new support ticket in the ticketing system and assigns it to a support agent"
    )
    public Ticket createTicket(
            @McpToolParam(description = "Full name of the person raising the ticket", required = true)
            String requestorName,

            @McpToolParam(description = "Detailed description of the issue", required = true)
            String ticketDescription,

            @McpToolParam(description = "Name of the company associated with the ticket", required = true)
            String companyName,

            @McpToolParam(description = "Numeric priority: 1=CRITICAL, 2=HIGH, 3=MEDIUM, 4=LOW", required = true)
            Integer priority,

            @McpToolParam(description = "Full display name of the support agent to assign", required = true)
            String assigneeName,

            @McpToolParam(description = "Email address of the support agent to assign", required = true)
            String assigneeEmail
    ) {

        log.info("Ticket Creation Tool called");
        Ticket request = new Ticket(
                requestorName, ticketDescription, companyName,
                priority, assigneeName, assigneeEmail,
                null, null
        );

        try {
            Ticket response = ticketApiClient.createTicket(request);
            response.setCreationDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            response.setStatus("success");
            return response;
        } catch (FeignException e) {
            throw new TicketCreationException(
                    "Ticket creation failed — API responded with HTTP " + e.status() +
                    ": " + e.contentUTF8(), e);
        } catch (Exception e) {
            throw new TicketCreationException(
                    "Ticket creation failed due to an unexpected error: " + e.getMessage(), e);
        }
    }
}
