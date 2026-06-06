package io.github.rafaviv.yakubackend.iam.domain.model.queries;

/**
 * Query to retrieve all users assigned to a specific farm.
 */
public record GetUsersByFarmIdQuery(Long farmId) {
    public GetUsersByFarmIdQuery {
        if (farmId == null) {
            throw new IllegalArgumentException("Farm ID cannot be null.");
        }
    }
}
