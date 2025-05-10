package org.polythec.projecthubbe.service;

import org.polythec.projecthubbe.dto.CommentDTO;
import org.polythec.projecthubbe.entity.Comment;
import org.polythec.projecthubbe.entity.Task;
import org.polythec.projecthubbe.entity.User;
import org.polythec.projecthubbe.repository.CommentRepository;
import org.polythec.projecthubbe.repository.TaskRepository;
import org.polythec.projecthubbe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public CommentService(CommentRepository commentRepository, TaskRepository taskRepository, UserRepository userRepository, NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * Get all comments for a specific task
     */
    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByTaskId(Long taskId) {
        List<Comment> comments = commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
        return comments.stream()
                .map(CommentDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Create a new comment
     */
    @Transactional
    public CommentDTO createComment(CommentDTO commentDTO) {
        // Use the userId already set in the DTO
        User currentUser = userRepository.findById(commentDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Task task = taskRepository.findById(commentDTO.getTaskId())
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + commentDTO.getTaskId()));

        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        comment.setTask(task);
        comment.setUser(currentUser);
        for (User assignee : task.getAssignees()) { // Assuming you have a 'Set<User> assignees' field in Task
            if (!assignee.getId().equals(currentUser.getId())) { // Optional: do not notify the author
                notificationService.sendNotification(assignee, "New comment on task: " + task.getTitle());
            }
        }

        Comment savedComment = commentRepository.save(comment);
        return new CommentDTO(savedComment);
    }

    /**
     * Update an existing comment
     */
    @Transactional
    public CommentDTO updateComment(Long commentId, CommentDTO commentDTO) {
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Find the comment
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with ID: " + commentId));

        // Check if the current user is the author of the comment
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("You can only edit your own comments");
        }

        // Update the comment
        comment.setContent(commentDTO.getContent());
        Comment updatedComment = commentRepository.save(comment);
        return new CommentDTO(updatedComment);
    }

    /**
     * Delete a comment
     */
    @Transactional
    public void deleteComment(Long commentId) {
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Find the comment
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with ID: " + commentId));

        // Check if the current user is the author of the comment
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("You can only delete your own comments");
        }

        // Delete the comment
        commentRepository.delete(comment);
    }

    /**
     * Get the count of comments for a task
     */
    @Transactional(readOnly = true)
    public Long getCommentCountForTask(Long taskId) {
        return commentRepository.countByTaskId(taskId);
    }
}