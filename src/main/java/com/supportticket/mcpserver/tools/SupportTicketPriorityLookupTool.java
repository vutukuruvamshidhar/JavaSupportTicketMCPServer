package com.supportticket.mcpserver.tools;

import com.supportticket.mcpserver.dto.CompanyPriority;
import com.supportticket.mcpserver.dto.PriorityResponse;
import com.supportticket.mcpserver.repository.SupportTicketRepo;
import com.supportticket.mcpserver.service.McpAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/**
 * MCP tool provider for support ticket operations.
 *
 * <p>Exposes tools that an AI assistant can invoke during ticket workflows.
 * Each tool method is discovered and registered automatically by the MCP
 * annotation scanner at application startup.</p>
 */
@Component
public class SupportTicketPriorityLookupTool {

    private static final Logger log = LoggerFactory.getLogger(SupportTicketPriorityLookupTool.class);

    private final SupportTicketRepo supportTicketRepo;
    private final McpAccessService mcpAccessService;

    public SupportTicketPriorityLookupTool(SupportTicketRepo supportTicketRepo,
                                            McpAccessService mcpAccessService) {
        this.supportTicketRepo = supportTicketRepo;
        this.mcpAccessService = mcpAccessService;
    }

    /**
     * Looks up the ticket priority for the given company name from the
     * {@code CompanyPriority} database table.
     *
     * <p>The priority is returned as a numeric value where lower numbers
     * indicate higher urgency:</p>
     * <ul>
     *   <li>1 — CRITICAL</li>
     *   <li>2 — HIGH</li>
     *   <li>3 — MEDIUM</li>
     *   <li>4 — LOW</li>
     * </ul>
     *
     * <p>If no record is found for the supplied company name, priority
     * defaults to {@code 3} (MEDIUM).</p>
     *
     * @param companyName the name of the company for which to resolve the priority
     * @return a {@link PriorityResponse} containing the resolved numeric priority
     */
    @McpTool(
            name = "lookupPriority",
            title = "Lookup Priority",
            description = "Resolves the ticket priority for a given company name"
    )
    public PriorityResponse lookupPriority(
            @McpToolParam(description = "Name of the company for which to resolve the ticket priority", required = true)
            String companyName
    ) {
        log.info("lookupPriority Tool called");
        mcpAccessService.requireToolAccess();
        Integer priority = supportTicketRepo.findByCompanyName(companyName)
                .map(CompanyPriority::getPriority)
                .orElse(3);
        return new PriorityResponse(priority);
    }
}
