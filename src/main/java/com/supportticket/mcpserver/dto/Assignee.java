package com.supportticket.mcpserver.dto;

/**
 * Represents a resolved support ticket assignee.
 */
public class Assignee {

    /** Unique ID for the user. */
    private String id;

    /** Full display name of the user. */
    private String displayName;

    /** Primary email address (UPN or mail) of the user. */
    private String email;

    /**
     * Creates an {@code Assignee} with the given ID, display name, and email.
     */
    public Assignee(String id, String displayName, String email) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }
}
