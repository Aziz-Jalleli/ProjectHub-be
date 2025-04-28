package org.polythec.projecthubbe.dto;

import lombok.Data;

@Data
public class UserSummaryDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    // No project references
    public UserSummaryDTO(org.polythec.projecthubbe.entity.User user) {
        this.id = user.getId();
        this.firstName = user.getUsername();
    }
    public UserSummaryDTO() {

    }
}
