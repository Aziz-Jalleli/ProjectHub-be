package org.polythec.projecthubbe.service;

import lombok.RequiredArgsConstructor;
import org.polythec.projecthubbe.entity.Task;
import org.polythec.projecthubbe.entity.Projet;
import org.polythec.projecthubbe.repository.TaskRepository;
import org.polythec.projecthubbe.repository.ProjetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjetRepository projetRepository;
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }



    public List<Task> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectIdprojet(projectId);
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public Task updateTask(Long id, Task updatedTask) {
        Optional<Task> existingTask = taskRepository.findById(id);
        if (existingTask.isPresent()) {
            Task task = existingTask.get();
            task.setTitle(updatedTask.getTitle());
            task.setType(updatedTask.getType());
            task.setStartDate(updatedTask.getStartDate());
            task.setEndDate(updatedTask.getEndDate());
            task.setDescription(updatedTask.getDescription());
            task.setUpdatedAt(updatedTask.getUpdatedAt());
            return taskRepository.save(task);
        }
        throw new IllegalArgumentException("Task not found");
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}
