package org.polythec.projecthubbe.service;

import lombok.RequiredArgsConstructor;
import org.polythec.projecthubbe.dto.NotificationDTO;
import org.polythec.projecthubbe.entity.Notification;
import org.polythec.projecthubbe.entity.User;
import org.polythec.projecthubbe.repository.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Notification sendNotification(User user, String message) {
        // Create and save notification in database
        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setMessage(message);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);

        // Send notification to WebSocket topic for this specific user
        NotificationDTO notificationDTO = convertToDTO(notification);
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + user.getId(),
                notificationDTO
        );
        return notification;
    }

    public List<NotificationDTO> getUserNotifications(String userId) {
        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(notification -> new NotificationDTO(
                        notification.getMessage(),
                        notification.getRecipient().getId(),
                        notification.getIsRead(),// Ensure this value is included
                        notification.getCreatedAt(),
                        notification.getId()
                ))
                .collect(Collectors.toList());
    }


    @Transactional
    public void markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        notificationOpt.ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);

            // Notify client about the status change
            NotificationDTO notificationDTO = convertToDTO(notification);
            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + notification.getRecipient().getId(),
                    notificationDTO
            );
        });
    }

    @Transactional
    public void markAllAsRead(String userId) {
        List<Notification> notifications = notificationRepository.findByRecipientIdAndIsReadFalse(userId);
        for (Notification notification : notifications) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }

        // Send update notification

    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setRecipientId(notification.getRecipient().getId());
        dto.setMessage(notification.getMessage());
        dto.setRecipientId(notification.getRecipient().getId());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setId(notification.getId());


        return dto;
    }
}