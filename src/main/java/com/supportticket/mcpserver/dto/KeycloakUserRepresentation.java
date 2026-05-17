package com.supportticket.mcpserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakUserRepresentation {

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() {
        String fn = firstName != null ? firstName : "";
        String ln = lastName != null ? lastName : "";
        String full = (fn + " " + ln).trim();
        return full.isEmpty() ? username : full;
    }
}