package io.github.rafaviv.yakubackend.iam.interfaces.rest.resources;

/**
 * Resource for authentication response
 * <p>
 * This record represents the data transfer object for authentication responses.
 * It includes the JWT token and user information for successful authentication.
 * </p>
 */
public record AuthenticationResponseResource(
    String token,
    String tokenType,
    Long expiresIn,
    UserResource user
) {
    
    public static AuthenticationResponseResource of(String token, Long expiresIn, UserResource user) {
        return new AuthenticationResponseResource(token, "Bearer", expiresIn, user);
    }
}

