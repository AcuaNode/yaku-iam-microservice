package io.github.rafaviv.yakubackend.iam.infrastructure.persistence.jpa.repositories;

import io.github.rafaviv.yakubackend.iam.domain.model.aggregates.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
/**
 * User Repository
 * <p>
 * This repository is responsible for managing User entities in the database.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find a user by username
     * @param username the username to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Check if a user exists by username
     * @param username the username to check
     * @return true if user exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Find all users by assigned farm ID
     * @param assignedFarmId the farm ID
     * @return List of users assigned to the farm
     */
    java.util.List<User> findAllByAssignedFarmId(Long assignedFarmId);
}