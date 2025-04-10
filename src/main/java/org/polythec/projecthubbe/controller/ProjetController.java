package org.polythec.projecthubbe.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.RequiredArgsConstructor;
import org.polythec.projecthubbe.entity.Projet;
import org.polythec.projecthubbe.service.ProjetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjetController {

    private final ProjetService projetService;

    @PostMapping("/create")
    public ResponseEntity<Projet> createProject(@RequestBody Projet projet) {
        return ResponseEntity.ok(projetService.createProject(projet));
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

    @PutMapping("/{projectId}/add-member/{userId}")
    public ResponseEntity<Projet> addMemberToProject(
            @PathVariable Long projectId,
            @PathVariable String userId) {
        return ResponseEntity.ok(projetService.addMember(projectId, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projetService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
