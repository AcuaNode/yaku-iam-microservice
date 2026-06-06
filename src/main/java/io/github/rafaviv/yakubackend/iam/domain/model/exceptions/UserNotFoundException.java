package io.github.rafaviv.yakubackend.iam.domain.model.exceptions;

/**
 * Exception thrown when a user is not found
 */
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String email) {
        super("User not found with email: " + email);
    }
    
    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

