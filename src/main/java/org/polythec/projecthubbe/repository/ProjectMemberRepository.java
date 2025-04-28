package org.polythec.projecthubbe.repository;

import org.polythec.projecthubbe.entity.ProjectMember;
import org.polythec.projecthubbe.entity.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    Optional<ProjectMember> findByProjectIdprojetAndUserIdEquals(Long projectId, String userId);
    List<ProjectMember> findByIdProjectId(Long projectId);
    void deleteByIdProjectIdAndIdUserId(Long projectId, String userId);

}
