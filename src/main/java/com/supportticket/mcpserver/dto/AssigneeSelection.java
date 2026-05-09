package com.supportticket.mcpserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Elicitation response DTO used when the user selects an assignee from a list
 * of candidates with the same display name.
 *
 * <p>The MCP framework serialises the JSON schema of this class into the
 * elicitation request so the client knows exactly what fields to collect.
 * The user provides the {@code selectedId} and {@code selectedName} of the
 * intended assignee, which the tool uses to resolve the final {@link Assignee}.</p>
 */
public class AssigneeSelection {

    /** Azure Entra ID object ID (GUID) of the chosen assignee. */
    @JsonProperty(required = true)
    private String selectedId;

    /** Full display name of the chosen assignee. */
    @JsonProperty(required = true)
    private String selectedName;

    /** No-arg constructor required for Jackson deserialisation. */
    public AssigneeSelection() {}

    /**
     * Returns the Azure Entra ID object ID of the selected assignee.
     *
     * @return the selected object ID
     */
    public String getSelectedId() {
        return selectedId;
    }

    /**
     * Sets the Azure Entra ID object ID of the selected assignee.
     *
     * @param selectedId the object ID to set
     */
    public void setSelectedId(String selectedId) {
        this.selectedId = selectedId;
    }

    /**
     * Returns the display name of the selected assignee.
     *
     * @return the selected display name
     */
    public String getSelectedName() {
        return selectedName;
    }

    /**
     * Sets the display name of the selected assignee.
     *
     * @param selectedName the display name to set
     */
    public void setSelectedName(String selectedName) {
        this.selectedName = selectedName;
    }
}
