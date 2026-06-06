package io.github.rafaviv.yakubackend.iam.domain.services;

import io.github.rafaviv.yakubackend.iam.domain.model.aggregates.User;
import io.github.rafaviv.yakubackend.iam.domain.model.queries.GetAllUsersQuery;
import io.github.rafaviv.yakubackend.iam.domain.model.queries.GetUserByUsernameQuery;

import java.util.List;
import java.util.Optional;

/**
 * Defines the contract for query-based (read-only) operations on the User aggregate.
 * Implementations are typically found in the Application layer.
 */
public interface UserQueryService {
    /**
     * Handles the query to find a user by username.
     * @param query The query containing the username.
     * @return An Optional containing the User aggregate if found.
     */
    Optional<User> handle(GetUserByUsernameQuery query);

    /**
     * Handles the query to retrieve all users.
     * @param query The query (empty, used for consistency).
     * @return A list of all User aggregates.
     */
    List<User> handle(GetAllUsersQuery query);

    /**
     * Handles the query to retrieve all users assigned to a specific farm.
     * @param query The query containing the farm ID.
     * @return A list of all User aggregates assigned to the farm.
     */
    List<User> handle(io.github.rafaviv.yakubackend.iam.domain.model.queries.GetUsersByFarmIdQuery query);
}
