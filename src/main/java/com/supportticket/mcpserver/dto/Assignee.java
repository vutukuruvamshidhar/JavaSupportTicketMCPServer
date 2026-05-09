package com.supportticket.mcpserver.dto;

/**
 * Represents a resolved support ticket assignee from Azure Entra ID.
 *
 * <p>Contains the Entra ID object ID, display name, and email address
 * for a user returned by the Microsoft Graph API.</p>
 */
public class Assignee {

    /** Azure Entra ID object ID (GUID) for the user. */
    private String id;

    /** Full display name of the user as stored in Entra ID. */
    private String displayName;

    /** Primary email address (UPN or mail) of the user. */
    private String email;

    /**
     * Creates an {@code Assignee} with the given ID, display name, and email.
     *
     * @param id          the Azure Entra ID object ID
     * @param displayName the user's full display name
     * @param email       the user's email address
     */
    public Assignee(String id, String displayName, String email) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
    }

    /**
     * Returns the Azure Entra ID object ID.
     *
     * @return the object ID (GUID)
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the user's full display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the user's email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }
}
