package com.supportticket.mcpserver.dto;

import com.supportticket.mcpserver.tools.SupportTicketPriorityLookupTool;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity representing a row in the {@code CompanyPriority} table.
 *
 * <p>Maps the company-to-priority relationship stored in the database, used
 * by {@link SupportTicketPriorityLookupTool} to resolve the ticket priority for a given
 * company at tool invocation time.</p>
 */
@Entity
@Table(name = "Company_Priority", schema = "Company_Database_Schema")
public class CompanyPriority {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long companyId;

    /** Name of the company. */
    @Column(name = "company_name", nullable = false)
    private String companyName;

    /** Numeric priority assigned to this company (1 = CRITICAL … 4 = LOW). */
    @Column(name = "priority", nullable = false)
    private Integer priority;

    /** No-arg constructor required by JPA. */
    public CompanyPriority() {}

    /**
     * Returns the company's primary key.
     *
     * @return the company ID
     */
    public Long getCompanyId() {
        return companyId;
    }

    /**
     * Returns the company name.
     *
     * @return the company name
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Sets the company name.
     *
     * @param companyName the company name to set
     */
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /**
     * Returns the numeric priority assigned to this company.
     *
     * @return the priority
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * Sets the numeric priority.
     *
     * @param priority the priority to set
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
