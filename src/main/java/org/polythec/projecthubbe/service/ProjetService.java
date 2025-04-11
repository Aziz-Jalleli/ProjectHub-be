package org.polythec.projecthubbe.service;

import lombok.RequiredArgsConstructor;
import org.polythec.projecthubbe.entity.Projet;
import org.polythec.projecthubbe.entity.User;
import org.polythec.projecthubbe.repository.ProjetRepository;
import org.polythec.projecthubbe.repository.UserRepository;
import org.polythec.projecthubbe.service.impl.UserServiceImpl;
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
}
