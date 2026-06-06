package io.github.rafaviv.yakubackend.iam.interfaces.rest.resources;

import java.util.Date;
import java.util.List;

/**
 * Resource for user information responses
 * <p>
 * This record represents the data transfer object for user information responses.
 * It provides a clean API response structure without exposing sensitive information
 * like password hashes.
 * </p>
 */
/**
 * Recurso para información de usuario
 * Solo contiene información de autenticación (IAM).
 * Los datos del cliente se manejan en el bounded context Cliente.
 */
public record UserResource(
    Long id,
    String username,
    String email,
    String firstName,
    String lastName,
    boolean isVerified,
    boolean active,
    List<String> roles,
    Date createdAt,
    Date updatedAt
) {
    
}

