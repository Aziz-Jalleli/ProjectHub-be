package org.polythec.projecthubbe.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private String message;
    private String recipientId;
}
