package io.github.rafaviv.yakubackend.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

/**
 * Resource for signing in.
 */
public record SignInResource(
    @NotBlank(message = "Username is required")
    String username,

    @NotBlank(message = "Password is required")
    String password
) {
    
}
