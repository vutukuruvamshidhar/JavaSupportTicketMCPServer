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
 * MCP tool that resolves the ticket priority for a given company name.
 *
 * <p>Looks up the company in the {@code CompanyPriority} database table and
 * returns the configured numeric priority. If no entry is found the tool
 * defaults to {@code 3} (MEDIUM). Access is guarded by
 * {@link McpAccessService#requireToolAccess()}.</p>
 */
@Component
public class SupportTicketPriorityLookupTool {

    private static final Logger log = LoggerFactory.getLogger(SupportTicketPriorityLookupTool.class);

    private final SupportTicketRepo supportTicketRepo;
    private final McpAccessService mcpAccessService;

    /**
     * Creates the tool with the given repository and access-control service.
     *
     * @param supportTicketRepo the JPA repository for company-priority lookups
     * @param mcpAccessService  the service that enforces JWT-based access checks
     */
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
