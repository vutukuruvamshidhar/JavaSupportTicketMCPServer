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

    /** Unique ID of the chosen assignee. */
    @JsonProperty(required = true)
    private String selectedId;

    /** Full display name of the chosen assignee. */
    @JsonProperty(required = true)
    private String selectedName;

    /** No-arg constructor required for Jackson deserialisation. */
    public AssigneeSelection() {}

    public String getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(String selectedId) {
        this.selectedId = selectedId;
    }

    public String getSelectedName() {
        return selectedName;
    }

    public void setSelectedName(String selectedName) {
        this.selectedName = selectedName;
    }
}
