package org.polythec.projecthubbe.repository;
import org.polythec.projecthubbe.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findById(Long id);

    public List<Task> findByProjectIdprojet(Long projectId);
    @Query("SELECT t FROM Task t JOIN t.assignees a WHERE a.id = :userId AND t.project.idprojet = :projectId")
    List<Task> findByAssigneesIdAndProjectIdprojet(@Param("userId") String userId, @Param("projectId") Long projectId);


}

