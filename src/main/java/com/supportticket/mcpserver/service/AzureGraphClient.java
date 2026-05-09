package com.supportticket.mcpserver.service;

import com.supportticket.mcpserver.dto.Assignee;

import java.util.List;

/**
 * Client interface for querying Azure Entra ID (Microsoft Graph API) to look
 * up users by display name.
 *
 * <p>Abstracting the Graph API behind this interface allows the tool layer to
 * remain testable without a live Azure connection.</p>
 */
public interface AzureGraphClient {

    /**
     * Searches for Entra ID users whose display name starts with the given value.
     *
     * @param displayName the display name (or prefix) to search for
     * @return a list of matching {@link Assignee} objects; empty if none found
     */
    List<Assignee> findByDisplayName(String displayName);
}
