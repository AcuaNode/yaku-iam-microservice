package io.github.rafaviv.yakubackend.iam.domain.model.commands;

/**
 * Command to sign in a user in the system.
 */
public record SignInCommand(
    String username,
    String password
) {
    public SignInCommand {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
    }
}