package org.polythec.projecthubbe.repository;

import org.polythec.projecthubbe.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Long> {
}
