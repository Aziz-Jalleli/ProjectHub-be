package org.polythec.projecthubbe.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ProjetDTO {
    private Long idprojet;
    private String nom;
    private String description;
    private LocalDateTime createdDate;
    private UserSummaryDTO owner;
    private Set<UserSummaryDTO> members;
    // No circular references
}
