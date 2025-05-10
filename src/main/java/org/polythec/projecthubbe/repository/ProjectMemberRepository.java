package org.polythec.projecthubbe.repository;

import org.polythec.projecthubbe.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByProjectIdprojet(Long projectId);

    List<ProjectMember> findByUserId(String userId);

    Optional<ProjectMember> findByProjectIdprojetAndUserId(Long projectId, String userId);

    boolean existsByProjectIdprojetAndUserId(Long projectId, String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProjectMember pm WHERE pm.project.idprojet = :projectId AND pm.user.id = :userId")
    void deleteByProjectIdprojetAndUserId(@Param("projectId") Long projectId, @Param("userId") String userId);
}