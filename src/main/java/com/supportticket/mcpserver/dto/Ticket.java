package com.supportticket.mcpserver.dto;

import com.fasterxml.jackson.annotation.JsonInclude;


/**
 * Represents a support ticket used as both the request body sent to the
 * ticketing API and the response returned to the MCP client.
 *
 * <p>Fields {@code creationDate} and {@code status} are {@code null} in
 * outbound requests and are populated in the response returned by
 * {@code TicketCreationTool} after a successful API call.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Ticket {

    /** Full name of the person who raised the ticket. */
    private String requestorName;

    /** Detailed description of the issue. */
    private String ticketDescription;

    /** Name of the company associated with this ticket. */
    private String companyName;

    /** Numeric priority level (1 = CRITICAL … 4 = LOW). */
    private Integer priority;

    /** Full display name of the assigned support agent. */
    private String assigneeName;

    /** Email address of the assigned support agent. */
    private String assigneeEmail;

    /** Timestamp when the ticket was created; set after a successful API response. */
    private String creationDate;

    /** Outcome of the creation attempt; {@code "success"} on success. */
    private String status;

    /** No-arg constructor required for Jackson deserialisation. */
    public Ticket() {}

    /**
     * Creates a fully-populated {@code Ticket} from all fields.
     *
     * @param requestorName     name of the requestor
     * @param ticketDescription issue description
     * @param companyName       company name
     * @param priority          numeric priority
     * @param assigneeName      assignee display name
     * @param assigneeEmail     assignee email
     * @param creationDate      creation timestamp
     * @param status            creation status
     */
    public Ticket(String requestorName, String ticketDescription, String companyName,
                  Integer priority, String assigneeName, String assigneeEmail,
                  String creationDate, String status) {
        this.requestorName = requestorName;
        this.ticketDescription = ticketDescription;
        this.companyName = companyName;
        this.priority = priority;
        this.assigneeName = assigneeName;
        this.assigneeEmail = assigneeEmail;
        this.creationDate = creationDate;
        this.status = status;
    }

    public String getRequestorName() { return requestorName; }
    public void setRequestorName(String requestorName) { this.requestorName = requestorName; }

    public String getTicketDescription() { return ticketDescription; }
    public void setTicketDescription(String ticketDescription) { this.ticketDescription = ticketDescription; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }

    public String getAssigneeEmail() { return assigneeEmail; }
    public void setAssigneeEmail(String assigneeEmail) { this.assigneeEmail = assigneeEmail; }

    public String getCreationDate() { return creationDate; }
    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
