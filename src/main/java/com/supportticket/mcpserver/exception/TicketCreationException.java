package com.supportticket.mcpserver.exception;

/**
 * Thrown when the ticketing API call fails during ticket creation.
 *
 * <p>Wraps underlying Feign or I/O exceptions so that callers receive a single,
 * consistent exception type with a human-readable message suitable for returning
 * to an MCP client.</p>
 */
public class TicketCreationException extends RuntimeException {

    /**
     * Creates a {@code TicketCreationException} with a descriptive message and
     * the underlying cause.
     *
     * @param message human-readable description of what went wrong
     * @param cause   the underlying exception
     */
    public TicketCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
