package io.github.rafaviv.yakubackend.iam.domain.model.exceptions;

/**
 * Exception thrown when attempting to create a user that already exists
 */
public class UserAlreadyExistsException extends RuntimeException {
    
    public UserAlreadyExistsException(String email) {
        super("User with email " + email + " already exists");
    }
    
    public UserAlreadyExistsException(String email, Throwable cause) {
        super("User with email " + email + " already exists", cause);
    }
}

