package org.polythec.projecthubbe.repository;
import org.polythec.projecthubbe.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findById(Long id);

    public List<Task> findByProjectIdprojet(Long projectId);


}

