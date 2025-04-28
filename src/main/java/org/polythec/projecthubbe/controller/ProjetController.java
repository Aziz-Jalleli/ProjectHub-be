package org.polythec.projecthubbe.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.RequiredArgsConstructor;
import org.polythec.projecthubbe.entity.Projet;
import org.polythec.projecthubbe.entity.User;
import org.polythec.projecthubbe.service.ProjetService;
import org.polythec.projecthubbe.service.UserService;
import org.polythec.projecthubbe.service.impl.UserServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PutMapping("/{id}")
    public ResponseEntity<Projet> updateProject(
            @PathVariable Long id,
            @RequestBody Projet projetDetails
    ) {
        Projet updatedProjet = projetService.updateProject(id, projetDetails);
        return ResponseEntity.ok(updatedProjet);
    }


    @JsonIgnore
    @GetMapping("/allprojects")
    public ResponseEntity<List<Projet>> getAllProjects() {
        return ResponseEntity.ok(projetService.getAllProjects());
    }

    @GetMapping("/owner/{userId}")
    public ResponseEntity<List<Projet>> getProjectsByOwner(@PathVariable String userId) {
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
}
