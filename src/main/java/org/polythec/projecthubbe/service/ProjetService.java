package org.polythec.projecthubbe.service;

import lombok.RequiredArgsConstructor;
import org.polythec.projecthubbe.entity.Projet;
import org.polythec.projecthubbe.entity.User;
import org.polythec.projecthubbe.repository.ProjetRepository;
import org.polythec.projecthubbe.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final UserRepository userRepository;

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

    public void deleteProject(Long id) {
        projetRepository.deleteById(id);
    }

    public Optional<Projet> getProjectById(Long id) {
        return projetRepository.findById(id);
    }
}
