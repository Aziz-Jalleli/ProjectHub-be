package org.polythec.projecthubbe.service;

import lombok.RequiredArgsConstructor;
import org.polythec.projecthubbe.entity.*;
import org.polythec.projecthubbe.repository.*;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjetRepository projetRepository;
    private final PriorityRepository priorityRepository;
    private final StatusRepository statusRepository;
    private final UserRepository userRepository;

    public Task createTask(Task task, Long priorityId, Long statusId) {
        if (priorityId != null) {
            Priority priority = priorityRepository.findById(priorityId)
                    .orElseThrow(() -> new IllegalArgumentException("Priority not found"));
            task.setPriority(priority);
            task.getPriorities().add(priority);
        }

        if (statusId != null) {
            Status status = statusRepository.findById(statusId)
                    .orElseThrow(() -> new IllegalArgumentException("Status not found"));
            task.setStatus(status);
        }

        return taskRepository.save(task);
    }

    public List<Task> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectIdprojet(projectId);
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public Task updateTask(Long id, Task updatedTask, Long priorityId, Long statusId) {
        Optional<Task> existingTask = taskRepository.findById(id);
        if (existingTask.isPresent()) {
            Task task = existingTask.get();

            if (updatedTask.getTitle() != null) task.setTitle(updatedTask.getTitle());
            if (updatedTask.getType() != null) task.setType(updatedTask.getType());
            if (updatedTask.getStartDate() != null) task.setStartDate(updatedTask.getStartDate());
            if (updatedTask.getEndDate() != null) task.setEndDate(updatedTask.getEndDate());
            if (updatedTask.getDescription() != null) task.setDescription(updatedTask.getDescription());
            if (updatedTask.getUpdatedAt() != null) task.setUpdatedAt(updatedTask.getUpdatedAt());

            if (priorityId != null) {
                Priority priority = priorityRepository.findById(priorityId)
                        .orElseThrow(() -> new IllegalArgumentException("Priority not found"));
                task.getPriorities().clear(); // Clear existing priorities
                task.getPriorities().add(priority);
            }

            if (statusId != null) {
                Status status = statusRepository.findById(statusId)
                        .orElseThrow(() -> new IllegalArgumentException("Status not found"));
                task.setStatus(status);
            }

            return taskRepository.save(task);
        }
        throw new IllegalArgumentException("Task not found");
    }


    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
    @Transactional
    public Task assignUserToTask(Long taskId, String userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if user is already assigned to this task
        if (task.getAssignees().stream().anyMatch(assignee -> assignee.getId().equals(userId))) {
            // User is already assigned - you can either throw an exception or ignore
            // Here we'll ignore and just return the task
            return task;
        }

        // Add user to assignees
        task.getAssignees().add(user);

        // Save and return the updated task
        return taskRepository.save(task);
    }
    public List<Task> getTasksByUserAndProject(String userId, Long projectId) {
        return taskRepository.findByAssigneesIdAndProjectIdprojet(userId, projectId);
    }

}