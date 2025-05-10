package org.polythec.projecthubbe.controller;

import org.polythec.projecthubbe.dto.CommentDTO;
import org.polythec.projecthubbe.dto.WebSocketMessage;
import org.polythec.projecthubbe.repository.UserRepository;
import org.polythec.projecthubbe.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class WebSocketCommentController {

    private final CommentService commentService;
    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    public WebSocketCommentController(CommentService commentService, SimpMessagingTemplate messagingTemplate) {
        this.commentService = commentService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handle new comment creation via WebSocket
     */
    @MessageMapping("/comments/create/{taskId}")
    @SendTo("/topic/comments/{taskId}")
    public CommentDTO createComment(@DestinationVariable Long taskId, CommentDTO commentDTO, Principal principal) {
        commentDTO.setTaskId(taskId);
        commentDTO.setUserId(extractUserId(principal));
        commentDTO.setCreatedAt(LocalDateTime.now());

        CommentDTO createdComment = commentService.createComment(commentDTO);

        // Broadcast the event to all subscribers
        WebSocketMessage<CommentDTO> message = new WebSocketMessage<>("COMMENT_CREATED", createdComment);
        messagingTemplate.convertAndSend("/topic/comments/" + taskId, message);

        return createdComment;
    }

    /**
     * Handle comment update via WebSocket
     */
    @MessageMapping("/comments/update/{taskId}/{commentId}")
    public void updateComment(@DestinationVariable Long taskId,
                              @DestinationVariable Long commentId,
                              CommentDTO commentDTO,
                              Principal principal) {
        commentDTO.setId(commentId);
        commentDTO.setTaskId(taskId);

        try {
            CommentDTO updatedComment = commentService.updateComment(commentId, commentDTO);

            // Broadcast the update event
            WebSocketMessage<CommentDTO> message = new WebSocketMessage<>("COMMENT_UPDATED", updatedComment);
            messagingTemplate.convertAndSend("/topic/comments/" + taskId, message);
        } catch (Exception e) {
            // Send error message back to sender
            WebSocketMessage<String> errorMessage = new WebSocketMessage<>("ERROR", e.getMessage());
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/errors", errorMessage);
        }
    }

    /**
     * Handle comment deletion via WebSocket
     */
    @MessageMapping("/comments/delete/{taskId}/{commentId}")
    public void deleteComment(@DestinationVariable Long taskId,
                              @DestinationVariable Long commentId,
                              Principal principal) {
        try {
            commentService.deleteComment(commentId);

            // Broadcast the deletion event
            WebSocketMessage<Long> message = new WebSocketMessage<>("COMMENT_DELETED", commentId);
            messagingTemplate.convertAndSend("/topic/comments/" + taskId, message);
        } catch (Exception e) {
            // Send error message back to sender
            WebSocketMessage<String> errorMessage = new WebSocketMessage<>("ERROR", e.getMessage());
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/errors", errorMessage);
        }
    }

    /**
     * Extract user ID from the Principal object
     */
    private String extractUserId(Principal principal) {
        String username = principal.getName(); // usually email
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalStateException("User not found"))
                .getId();
    }

    /**
     * Helper method to extract user ID from principal
     * Modify this according to your user details implementation
     */
    private String getUserIdFromPrincipal(Object userDetails) {
        // Implement based on your UserDetails implementation
        // Example:
         if (userDetails instanceof org.springframework.security.core.userdetails.UserDetails) {
             String username = ((org.springframework.security.core.userdetails.UserDetails) userDetails).getUsername();
             return userRepository.findByEmail(username)
                     .orElseThrow(() -> new IllegalStateException("User not found"))
                     .getId();
         }
        throw new UnsupportedOperationException("User ID extraction not implemented");
    }
}