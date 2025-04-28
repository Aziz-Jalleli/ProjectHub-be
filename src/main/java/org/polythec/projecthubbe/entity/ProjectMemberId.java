package org.polythec.projecthubbe.entity;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Embeddable
public class ProjectMemberId implements Serializable {
    // Getters and Setters
    private Long projectId;
    private String userId;

    // Default constructor
    public ProjectMemberId() {}

    // Constructor with fields
    public ProjectMemberId(Long projectId, String userId) {
        this.projectId = projectId;
        this.userId = userId;
    }

    // Override equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectMemberId that = (ProjectMemberId) o;
        return Objects.equals(projectId, that.projectId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, userId);
    }
}