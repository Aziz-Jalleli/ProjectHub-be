package org.polythec.projecthubbe.repository;

import org.polythec.projecthubbe.entity.Priority;
import org.polythec.projecthubbe.entity.Projet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PriorityRepository extends JpaRepository<Priority, Long> {

}
