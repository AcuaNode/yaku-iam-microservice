package io.github.rafaviv.yakubackend.iam.interfaces.rest.transform;

import io.github.rafaviv.yakubackend.iam.domain.model.aggregates.User;
import io.github.rafaviv.yakubackend.iam.interfaces.rest.resources.UserResource;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Assembler for converting User entity to UserResource
 * <p>
 * This class provides static methods to transform data between the domain layer
 * (entities) and the interface layer (REST resources) following DDD principles.
 * </p>
 */
public class UserResourceFromEntityAssembler {
    
    /**
     * Converts a User entity to a UserResource
     * @param user the User entity from the domain
     * @return UserResource for REST response
     */
    public static UserResource toResourceFromEntity(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        
        return new UserResource(
                user.getId(),
                user.getUsername(),
                user.getEmailAddress(),
                user.getFirstName(),
                user.getLastName(),
                user.isVerified(),
                user.getActive(),
                roleNames,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
