package com.supportticket.mcpserver.repository;

import com.supportticket.mcpserver.dto.CompanyPriority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link CompanyPriority} entity.
 *
 * <p>Provides CRUD operations against the {@code CompanyPriority} table and
 * exposes a derived query for looking up a company's priority by name.</p>
 */
public interface SupportTicketRepo extends JpaRepository<CompanyPriority, Long> {

    /**
     * Finds the {@link CompanyPriority} record whose {@code company_name}
     * matches the supplied value (case-sensitive).
     *
     * @param companyName the company name to search for
     * @return an {@link Optional} containing the matching record, or empty if
     *         none exists
     */
    Optional<CompanyPriority> findByCompanyName(String companyName);
}
