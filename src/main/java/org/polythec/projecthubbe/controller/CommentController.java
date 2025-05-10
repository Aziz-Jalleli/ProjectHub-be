package org.polythec.projecthubbe.controller;

import org.polythec.projecthubbe.dto.CommentDTO;
import org.polythec.projecthubbe.dto.WebSocketMessage;
import org.polythec.projecthubbe.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final SimpMessagingTemplate messagingTemplate;


    @Autowired
    public CommentController(CommentService commentService, SimpMessagingTemplate messagingTemplate) {
        this.commentService = commentService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Get all comments for a specific task
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByTaskId(@PathVariable Long taskId) {
        List<CommentDTO> comments = commentService.getCommentsByTaskId(taskId);
        return ResponseEntity.ok(comments);
    }

    /**
     * Create a new comment
     */
    @PostMapping
    public ResponseEntity<CommentDTO> createComment(@Valid @RequestBody CommentDTO commentDTO) {
        CommentDTO createdComment = commentService.createComment(commentDTO);
        WebSocketMessage<CommentDTO> message = new WebSocketMessage<>("COMMENT_CREATED", createdComment);
        messagingTemplate.convertAndSend("/topic/comments/" + createdComment.getTaskId(), message);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    /**
     * Update an existing comment
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentDTO commentDTO) {
        try {
            CommentDTO updatedComment = commentService.updateComment(commentId, commentDTO);
            WebSocketMessage<CommentDTO> message = new WebSocketMessage<>("COMMENT_UPDATED", updatedComment);
            messagingTemplate.convertAndSend("/topic/comments/" + updatedComment.getTaskId(), message);
            return ResponseEntity.ok(updatedComment);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Delete a comment
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        try {
            commentService.deleteComment(commentId);
            WebSocketMessage<Long> message = new WebSocketMessage<>("COMMENT_DELETED", commentId);
            messagingTemplate.convertAndSend("/topic/comments/" + commentId, message);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Get comment count for a task
     */
    @GetMapping("/count/task/{taskId}")
    public ResponseEntity<Long> getCommentCountForTask(@PathVariable Long taskId) {
        Long count = commentService.getCommentCountForTask(taskId);
        return ResponseEntity.ok(count);
    }

}