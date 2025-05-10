package org.polythec.projecthubbe.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.polythec.projecthubbe.dto.ProjetDTO;
import org.polythec.projecthubbe.dto.UserSummaryDTO;
import org.polythec.projecthubbe.entity.ProjectMember;
import org.polythec.projecthubbe.entity.Projet;
import org.polythec.projecthubbe.entity.Task;
import org.polythec.projecthubbe.entity.User;
import org.polythec.projecthubbe.repository.ProjectMemberRepository;
import org.polythec.projecthubbe.repository.ProjetRepository;
import org.polythec.projecthubbe.repository.TaskRepository;
import org.polythec.projecthubbe.repository.UserRepository;
import org.polythec.projecthubbe.service.impl.UserServiceImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final UserServiceImpl userService;

    public Projet createProject(Projet projet) {
        Projet savedProjet = projetRepository.save(projet);
        // Add owner as a member with admin role
        addUserToProject(savedProjet.getIdprojet(), savedProjet.getOwner().getId(), "Admin");
        return savedProjet;
    }

    @Transactional
    public void addUserToProject(Long projectId, String userId, String role) {
        Projet projet = projetRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Check if the user is already a member
        boolean isAlreadyMember = projectMemberRepository.existsByProjectIdprojetAndUserId(projectId, userId);

        if (!isAlreadyMember) {
            ProjectMember projectMember = new ProjectMember();
            projectMember.setProject(projet);
            projectMember.setUser(user);
            projectMember.setRole(role);
            projectMemberRepository.save(projectMember);
        }
    }

    public Projet updateProject(Long id, Projet projetDetails) {
        return projetRepository.findById(id)
                .map(projet -> {
                    if (projetDetails.getNom() != null) {
                        projet.setNom(projetDetails.getNom());
                    }
                    if (projetDetails.getDescription() != null) {
                        projet.setDescription(projetDetails.getDescription());
                    }
                    // Update other fields as needed
                    return projetRepository.save(projet);
                })
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
    }

    public List<Projet> getAllProjects() {
        return projetRepository.findAll();
    }

    public List<ProjetDTO> getProjectsByOwner(String userId) {
        List<Projet> projects = projetRepository.findByOwnerId(userId);

        return projects.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ProjetDTO convertToDTO(Projet projet) {
        ProjetDTO dto = new ProjetDTO();
        dto.setIdprojet(projet.getIdprojet());
        dto.setNom(projet.getNom());
        dto.setDescription(projet.getDescription());
        dto.setCreatedDate(projet.getCreatedDate());

        User user = projet.getOwner();
        UserSummaryDTO userSummaryDTO = userService.mapToSDTO(user);

        dto.setOwner(userSummaryDTO);
        return dto;
    }

    public Projet addMember(Long projectId, String userId) {
        Projet projet = projetRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // The logic to add the member to the project
        // ...

        return projet;
    }

    public void deleteProject(Long id) {
        projetRepository.deleteById(id);
    }

    @Transactional
    public void deleteProjectWithRelatedData(Long projectId, String currentUserId) {
        // Fetch the project
        Projet project = projetRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));

        // Check if the current user is the project owner
        if (!project.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Only the project owner can delete this project");
        }

        // Delete all tasks associated with this project
        List<Task> tasks = taskRepository.findByProjectIdprojet(projectId);
        if (!tasks.isEmpty()) {
            // For each task, clear associations first
            for (Task task : tasks) {
                // Clear assignees for each task before deleting
                task.getAssignees().clear();
                task.getPriorities().clear();
                task.setStatus(null);
                taskRepository.save(task);
            }
            taskRepository.deleteAll(tasks);
        }

        // Delete all project members
        List<ProjectMember> members = projectMemberRepository.findByProjectIdprojet(projectId);


        // Finally delete the project
        projetRepository.deleteById(projectId);
    }

    public List<ProjectMember> getMembersByProjectId(Long projectId) {
        return projectMemberRepository.findByProjectIdprojet(projectId);
    }

    public void deleteMemberFromProject(Long projectId, String userId) {
        ProjectMember member = projectMemberRepository.findByProjectIdprojetAndUserId(projectId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found in this project"));
        List<Task> tasks = taskRepository.findByProjectIdprojet(projectId);
        for (Task task : tasks) {
            if (task.getAssignees().removeIf(assignee -> assignee.getId().equals(userId))) {
                taskRepository.save(task); // Save only if an assignee was actually removed
            }
        }
        projectMemberRepository.delete(member);
    }

    public Optional<Projet> getProjectById(Long id) {
        return projetRepository.findById(id);
    }

    public List<ProjetDTO> getProjectsByMember(String userId) {
        List<ProjectMember> memberships = projectMemberRepository.findByUserId(userId);

        List<ProjetDTO> projects = new ArrayList<>();
        for (ProjectMember membership : memberships) {
            Projet projet = membership.getProject();
            projects.add(convertToDTO(projet));
        }

        return projects;
    }
}