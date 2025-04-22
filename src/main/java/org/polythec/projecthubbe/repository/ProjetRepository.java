package org.polythec.projecthubbe.repository;

import org.polythec.projecthubbe.entity.Projet;
import org.polythec.projecthubbe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjetRepository extends JpaRepository<Projet, Long> {

    // Find all projects owned by a specific user
    List<Projet> findByOwner(User owner);

    // Find all projects where a specific user is a member
    List<Projet> findByMembersContaining(User member);

    // Optional: Search projects by name
    List<Projet> findByNomContainingIgnoreCase(String nom);

    List<Projet> findByOwner_Email(String email);

}
