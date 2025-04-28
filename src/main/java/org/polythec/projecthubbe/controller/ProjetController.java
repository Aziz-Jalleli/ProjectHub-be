package org.polythec.projecthubbe.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.EntityNotFoundException;
import org.polythec.projecthubbe.dto.ProjectMemberDTO;
import org.polythec.projecthubbe.dto.ProjetDTO;
import org.polythec.projecthubbe.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.polythec.projecthubbe.entity.ProjectMember;
import org.polythec.projecthubbe.entity.Projet;
import org.polythec.projecthubbe.entity.User;
import org.polythec.projecthubbe.service.ProjetService;
import org.polythec.projecthubbe.service.UserService;
import org.polythec.projecthubbe.service.impl.UserServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()") // Optional but helpful for debugging
public class ProjetController {

    private final ProjetService projetService;
    private final UserServiceImpl userServiceImpl;

    @PostMapping("/create")
    public ResponseEntity<Projet> createProject(@RequestBody Projet projet) {
        User loggedInUser = userServiceImpl.getCurrentlyAuthenticatedUser(); // ✅ correct call
        projet.setOwner(loggedInUser);
        return ResponseEntity.ok(projetService.createProject(projet));
    }


    @JsonIgnore
    @GetMapping("/allprojects")
    public ResponseEntity<List<Projet>> getAllProjects() {
        return ResponseEntity.ok(projetService.getAllProjects());
    }

    @GetMapping("/owner/{userId}")
    public ResponseEntity<List<ProjetDTO>> getProjectsByOwner(@PathVariable String userId) {
        return ResponseEntity.ok(projetService.getProjectsByOwner(userId));
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<?> addUserToProject(
            @PathVariable Long projectId,
            @RequestParam String userId,
            @RequestParam String role
    ) {
        projetService.addUserToProject(projectId, userId, role);
        return ResponseEntity.ok("User added to project with role: " + role);
    }

    @PutMapping("/{projectId}/add-member/{userId}")
    public ResponseEntity<Projet> addMemberToProject(
            @PathVariable Long projectId,
            @PathVariable String userId) {  // Keep as String
        return ResponseEntity.ok(projetService.addMember(projectId, userId));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projetService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping(value = "/{projectId}/members", produces = "application/json")
    public List<ProjectMemberDTO> getMembers(@PathVariable Long projectId) {
        List<ProjectMember> members = projetService.getMembersByProjectId(projectId);
        return members.stream().map(member -> {
            ProjectMemberDTO dto = new ProjectMemberDTO();
            dto.setId(member.getUser().getId());
            dto.setEmail(member.getUser().getEmail());
            dto.setFirstName(member.getUser().getFirstName());
            dto.setLastName(member.getUser().getLastName());
            dto.setProfilePicture(member.getUser().getProfilePicture());
            dto.setRole(member.getRole());
            return dto;
        }).collect(Collectors.toList());
    }
    @DeleteMapping(value = "/{projectId}/members/{userId}", produces = "application/json")
    public ResponseEntity<String> deleteMember(@PathVariable Long projectId, @PathVariable String userId) {
        try {
            projetService.deleteMemberFromProject(projectId, userId);
            return ResponseEntity.ok("Member with userId " + userId + " removed from project " + projectId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<Optional<Projet>> getProjectById(@PathVariable Long id) {
        Optional<Projet> project = projetService.getProjectById(id);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(project);
    }


}
