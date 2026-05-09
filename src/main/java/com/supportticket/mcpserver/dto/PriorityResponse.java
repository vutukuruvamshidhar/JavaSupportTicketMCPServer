package com.supportticket.mcpserver.dto;

/**
 * Response object returned by the priority lookup MCP tool.
 *
 * <p>Carries the resolved ticket priority for a given company, where lower
 * numeric values indicate higher urgency (e.g. 1 = CRITICAL, 4 = LOW).</p>
 */
public class PriorityResponse {

    /** Numeric priority level resolved for the company. */
    private Integer priority;

    /**
     * Creates a {@code PriorityResponse} with the given priority.
     *
     * @param priority the resolved numeric priority level
     */
    public PriorityResponse(Integer priority) {
        this.priority = priority;
    }

    /**
     * Returns the numeric priority level.
     *
     * @return the priority
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * Sets the numeric priority level.
     *
     * @param priority the priority to set
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
