package org.polythec.projecthubbe.dto;

import lombok.Data;

@Data
public class AssigneeDTO {
    private String id;
    private String name; // Optional, include if frontend needs to display names
}