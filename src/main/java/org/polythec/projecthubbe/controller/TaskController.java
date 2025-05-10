package org.polythec.projecthubbe.controller;

import lombok.RequiredArgsConstructor;
import org.polythec.projecthubbe.dto.TaskDTO;
import org.polythec.projecthubbe.entity.Task;
import org.polythec.projecthubbe.entity.User;
import org.polythec.projecthubbe.service.TaskService;
import org.polythec.projecthubbe.service.impl.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final UserServiceImpl userServiceImpl;

    @PostMapping("/create")
    public ResponseEntity<?> createTask(
            @RequestBody Task task,
            @RequestParam(value = "priorityId", required = true) Long priorityId,
            @RequestParam("statusId") Long statusId) {
        try {
            Task createdTask = taskService.createTask(task, priorityId, statusId);
            return ResponseEntity.ok(createdTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskDTO>> getTasksByProject(@PathVariable Long projectId) {
        List<Task> tasks = taskService.getTasksByProject(projectId);

        List<TaskDTO> taskDTOs = tasks.stream()
                .map(TaskDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(taskDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Optional<Task> task = taskService.getTaskById(id);
        return task.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @RequestBody Task updatedTask,
            @RequestParam(required = false) Long priorityId,
            @RequestParam(required = false) Long statusId
    ) {
        try {
            Task task = taskService.updateTask(id, updatedTask, priorityId, statusId);
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Get current user ID from authentication
            User currentUser = userServiceImpl.getCurrentlyAuthenticatedUser();
            String currentUserId = currentUser.getId();
            // Delete task with permission check
            taskService.deleteTask(id, currentUserId);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body( e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/{taskId}/assignees/{userId}")
    public ResponseEntity<Task> assignUserToTask(
            @PathVariable Long taskId,
            @PathVariable String userId) {
        try {
            Task updatedTask = taskService.assignUserToTask(taskId, userId);
            return new ResponseEntity<>(updatedTask, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/user/{userId}/project/{projectId}")
    public List<Task> getTasksByUserAndProject(@PathVariable String userId, @PathVariable Long projectId) {
        return taskService.getTasksByUserAndProject(userId, projectId);
    }
}
