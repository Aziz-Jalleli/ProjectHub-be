package org.polythec.projecthubbe.repository;

import jakarta.persistence.LockModeType;
import org.polythec.projecthubbe.entity.ProjectMember;
import org.polythec.projecthubbe.entity.Projet;
import org.polythec.projecthubbe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjetRepository extends JpaRepository<Projet, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    // Find all projects owned by a specific user
    List<Projet> findByOwner(User owner);

    // Find all projects where a specific user is a member
    // Optional: Search projects by name
    List<Projet> findByNomContainingIgnoreCase(String nom);

    List<Projet> findByOwner_Email(String email);
    @Query("SELECT p FROM Projet p WHERE p.owner.id = :ownerId")
    List<Projet> findProjectsByOwnerId(@Param("ownerId") String ownerId);


    List<Projet> findByOwnerId(String ownerId);
}
