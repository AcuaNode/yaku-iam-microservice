package io.github.rafaviv.yakubackend.iam.domain.services;

import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.Roles;

/**
 * Service for role validation operations
 * This service provides business logic for validating role-related operations
 * in the IAM domain.
 */
public interface RoleValidationService {
    
    /**
     * Validates if a role can be requested during registration
     * @param role the role to validate
     * @return true if the role can be requested, false otherwise
     */
    boolean canRequestRole(Roles role);
    
    /**
     * Gets all available roles that can be requested during registration
     * @return array of available roles
     */
    Roles[] getAvailableRolesForRegistration();
}

