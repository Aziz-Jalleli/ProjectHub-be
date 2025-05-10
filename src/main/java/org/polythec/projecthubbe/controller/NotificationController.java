package org.polythec.projecthubbe.controller;

import lombok.RequiredArgsConstructor;
import org.polythec.projecthubbe.dto.NotificationDTO;
import org.polythec.projecthubbe.entity.Notification;
import org.polythec.projecthubbe.service.NotificationService;
import org.polythec.projecthubbe.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/{userId}")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @PostMapping("/mark-read/{notificationId}")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mark-all-read/{userId}")
    public ResponseEntity<?> markAllNotificationsAsRead(@PathVariable String userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    // For testing purposes - allows sending notifications via REST API
    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationDTO notificationDTO) {
        notificationService.sendNotification(
                userService.getUserById(notificationDTO.getRecipientId()),
                notificationDTO.getMessage()
        );
        return ResponseEntity.ok().build();
    }

    // WebSocket endpoint to send notifications
    @MessageMapping("/notify")
    public void processNotification(@Payload NotificationDTO notificationDTO) {
        Notification notification = notificationService.sendNotification(
                userService.getUserById(notificationDTO.getRecipientId()),
                notificationDTO.getMessage()
        );
        // Send notification to user-specific queue
        messagingTemplate.convertAndSend(
                "/user/" + notificationDTO.getRecipientId() + "/notifications",
                notification
        );
    }
}