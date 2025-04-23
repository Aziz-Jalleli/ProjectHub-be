package org.polythec.projecthubbe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberId implements java.io.Serializable {
    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "user_id")
    private String userId;
}

