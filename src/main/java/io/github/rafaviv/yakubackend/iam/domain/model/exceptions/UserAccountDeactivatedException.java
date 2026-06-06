package io.github.rafaviv.yakubackend.iam.domain.model.exceptions;

/**
 * Exception thrown when attempting to authenticate with a deactivated user account
 */
public class UserAccountDeactivatedException extends RuntimeException {
    
    public UserAccountDeactivatedException(String email) {
        super("User account with email " + email + " is deactivated");
    }
    
    public UserAccountDeactivatedException(Long userId) {
        super("User account with ID " + userId + " is deactivated");
    }
    
    public UserAccountDeactivatedException(String message, Throwable cause) {
        super(message, cause);
    }
}

