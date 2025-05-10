package org.polythec.projecthubbe.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private String message;
    private String recipientId;
    private boolean isRead; // ⬅️ ADD this line
    private LocalDateTime createdAt;
    private Long id;

    public NotificationDTO(String message, String id, Boolean isRead, Object o, LocalDateTime createdAt) {
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
}
