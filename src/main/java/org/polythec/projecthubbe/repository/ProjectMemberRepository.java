package org.polythec.projecthubbe.repository;

import org.polythec.projecthubbe.entity.ProjectMember;
import org.polythec.projecthubbe.entity.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    // You can add custom queries here if needed
}
