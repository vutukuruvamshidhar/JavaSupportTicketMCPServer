package com.supportticket.mcpserver.apiclient;

import com.supportticket.mcpserver.dto.Ticket;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for the ticketing REST API.
 *
 * <p>The base URL is resolved from the {@code ticketing.api.base-url} application
 * property at startup, allowing it to be overridden per environment without code
 * changes.</p>
 */
@FeignClient(name = "ticket-api", url = "${ticketing.api.base-url}")
public interface TicketApiClient {

    /**
     * Creates a new support ticket by calling {@code POST /ticket}.
     *
     * @param ticket the ticket details to persist
     * @return the created {@link Ticket} as returned by the API, including any
     *         server-assigned fields such as a ticket ID
     */
    @PostMapping("/ticket")
    Ticket createTicket(@RequestBody Ticket ticket);
}
