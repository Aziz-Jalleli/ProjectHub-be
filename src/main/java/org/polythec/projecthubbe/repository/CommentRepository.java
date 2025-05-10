package org.polythec.projecthubbe.repository;

import org.polythec.projecthubbe.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Find all comments for a specific task
    List<Comment> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    // Count comments for a specific task
    Long countByTaskId(Long taskId);
}