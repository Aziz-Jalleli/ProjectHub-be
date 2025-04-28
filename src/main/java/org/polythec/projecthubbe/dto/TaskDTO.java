package org.polythec.projecthubbe.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long projectId;
    private String projectName;

    private Long statusId;
    private String statusName;

    private Long priorityId;
    private String priorityName;

    private Set<UserSummaryDTO> assignees;

    public TaskDTO(org.polythec.projecthubbe.entity.Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.type = task.getType();
        this.startDate = task.getStartDate();
        this.endDate = task.getEndDate();
        this.description = task.getDescription();
        this.createdAt = task.getCreatedAt();
        this.updatedAt = task.getUpdatedAt();

        if (task.getProject() != null) {
            this.projectId = task.getProject().getIdprojet();
            this.projectName = task.getProject().getNom();
        }

        if (task.getStatus() != null) {
            this.statusId = task.getStatus().getId();
            this.statusName = task.getStatus().getName();
        }

        if (task.getPriority() != null) {
            this.priorityId = task.getPriority().getId();
            this.priorityName = task.getPriority().getName();
        }

        // Assignees -> Set<UserSummaryDTO>
        if (task.getAssignees() != null) {
            this.assignees = task.getAssignees().stream()
                    .map(UserSummaryDTO::new) // Tu dois avoir un constructeur UserSummaryDTO(User user)
                    .collect(java.util.stream.Collectors.toSet());
        }
    }

} 

