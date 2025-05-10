package org.polythec.projecthubbe.dto;

import lombok.Data;
import lombok.Setter;
import org.polythec.projecthubbe.entity.Comment;

import java.time.LocalDateTime;

@Data
public class CommentDTO {

    private Long id;
    private String content;
    private Long taskId;
    @Setter
    private String userId;
    private UserDTO user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public CommentDTO() {}

    // Constructor to convert Comment entity to DTO
    public CommentDTO(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();

        if (comment.getTask() != null) {
            this.taskId = comment.getTask().getId();
        }

        if (comment.getUser() != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(comment.getUser().getId());
            userDTO.setFirstName(comment.getUser().getFirstName());
            userDTO.setLastName(comment.getUser().getLastName());
            userDTO.setEmail(comment.getUser().getEmail());
            userDTO.setProfilePicture(comment.getUser().getProfilePicture());
            this.user = userDTO;
        }

        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
    }

}