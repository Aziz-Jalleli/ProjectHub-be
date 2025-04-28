package org.polythec.projecthubbe.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.polythec.projecthubbe.entity.ProjectMember;
import org.polythec.projecthubbe.entity.ProjectMemberId;
import org.polythec.projecthubbe.entity.Projet;
import org.polythec.projecthubbe.entity.User;
import org.polythec.projecthubbe.repository.ProjectMemberRepository;
import org.polythec.projecthubbe.repository.ProjetRepository;
import org.polythec.projecthubbe.repository.UserRepository;
import org.polythec.projecthubbe.service.impl.UserServiceImpl;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final UserRepository userRepository;
    private final UserServiceImpl userService;

    public List<Projet> getProjectsByOwnerEmail(String email) {
        return projetRepository.findByOwner_Email(email);
    }


    public Projet createProject(Projet projet) {
        return projetRepository.save(projet);
    }

    public List<Projet> getAllProjects() {
        return projetRepository.findAll();
    }

    public List<Projet> getProjectsByOwner(String userId) {
        Optional<User> owner = userRepository.findById(userId);
        return owner.map(projetRepository::findByOwner).orElse(List.of());
    }

    public Projet addMember(Long projectId, String userId) {
        Optional<Projet> optionalProjet = projetRepository.findById(projectId);
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalProjet.isPresent() && optionalUser.isPresent()) {
            Projet projet = optionalProjet.get();
            projet.addMember(optionalUser.get());
            return projetRepository.save(projet);
        }
        throw new IllegalArgumentException("Project or User not found");
    }

    public void deleteProject(Long projectId) {
        Projet projet = projetRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User currentUser = userService.getCurrentlyAuthenticatedUser();

        // Compare String IDs
        if (!projet.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the owner can delete this project");
        }

        projetRepository.delete(projet);
    }
    public Optional<Projet> getProjectById(Long id) {
        return projetRepository.findById(id);
    }
    private final ProjectMemberRepository projectMemberRepository;

    @Transactional
    public void addUserToProject(Long projectId, String userId, String role) {
        // Find the project
        Projet project = projetRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if the user is already a member
        Optional<ProjectMember> existingMembership = projectMemberRepository.findByProjectIdprojetAndUserIdEquals(projectId, userId);

        if (existingMembership.isPresent()) {
            // Update role if the member already exists
            ProjectMember member = existingMembership.get();
            member.setRole(role);
            projectMemberRepository.saveAndFlush(member);
        } else {
            // Create new project membership
            ProjectMember projectMember = new ProjectMember();
            ProjectMemberId memberId = new ProjectMemberId(projectId, userId);

            projectMember.setId(memberId);
            projectMember.setProject(project);
            projectMember.setUser(user);
            projectMember.setRole(role);

            // Save the membership
            projectMemberRepository.saveAndFlush(projectMember);

            // Update the collections on both sides for better consistency
            project.getProjectMembers().add(projectMember);
            project.getMembers().add(user);
        }
    }

    public Projet updateProject(Long id, Projet projetDetails) {
        Projet existingProjet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        // Update only non-null fields
        if (projetDetails.getNom() != null && !projetDetails.getNom().isBlank()) {
            existingProjet.setNom(projetDetails.getNom().trim());
        }

        if (projetDetails.getDescription() != null) {
            existingProjet.setDescription(projetDetails.getDescription().trim());
        }

        // Add owner validation if needed
        User currentUser = userService.getCurrentlyAuthenticatedUser();
        if (!existingProjet.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the owner can update this project");
        }

        return projetRepository.save(existingProjet);
    }
}
