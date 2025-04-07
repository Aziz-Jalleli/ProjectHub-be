package org.polythec.projecthubbe.repository;

import org.polythec.projecthubbe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Find a user by their email address
     *
     * @param email the email to search for
     * @return an Optional containing the user if found, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given email exists
     *
     * @param email the email to check
     * @return true if a user with the email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find users by their verification status
     *
     * @param isVerified the verification status to search for
     * @return a list of users with the specified verification status
     */
    List<User> findByIsVerified(Boolean isVerified);

    /**
     * Find users by their status
     *
     * @param status the status to search for (active, inactive, banned)
     * @return a list of users with the specified status
     */
    List<User> findByStatus(String status);
}
