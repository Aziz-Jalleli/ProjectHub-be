package org.polythec.projecthubbe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserDTO {
    private String id;
    private String email;
    @JsonProperty("first_name") // Add this annotation
    private String firstName;
    @JsonProperty("last_name") // Add this annotation

    private String lastName;
    private String profilePicture;
    private String role;
    private String cloudinaryPublicId; // Add this field

    // Add getter and setter
    public String getCloudinaryPublicId() {
        return cloudinaryPublicId;
    }

    public void setCloudinaryPublicId(String cloudinaryPublicId) {
        this.cloudinaryPublicId = cloudinaryPublicId;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}