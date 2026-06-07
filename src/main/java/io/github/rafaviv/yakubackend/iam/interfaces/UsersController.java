package io.github.rafaviv.yakubackend.iam.interfaces;

import io.github.rafaviv.yakubackend.iam.application.internal.commandservices.UserCommandServiceImpl;
import io.github.rafaviv.yakubackend.iam.application.internal.queryservices.UserQueryServiceImpl;
import io.github.rafaviv.yakubackend.iam.domain.model.aggregates.User;
import io.github.rafaviv.yakubackend.iam.domain.model.commands.SignInCommand;
import io.github.rafaviv.yakubackend.iam.domain.model.commands.SignUpCommand;
import io.github.rafaviv.yakubackend.iam.domain.model.exceptions.InvalidCredentialsException;
import io.github.rafaviv.yakubackend.iam.domain.model.exceptions.UserAccountDeactivatedException;
import io.github.rafaviv.yakubackend.iam.domain.model.exceptions.UserAlreadyExistsException;
import io.github.rafaviv.yakubackend.iam.domain.model.exceptions.UserNotFoundException;
import io.github.rafaviv.yakubackend.iam.domain.model.queries.GetAllUsersQuery;
import io.github.rafaviv.yakubackend.iam.domain.model.queries.GetUserByIdQuery;
import io.github.rafaviv.yakubackend.iam.domain.model.queries.GetUserByUsernameQuery;
import io.github.rafaviv.yakubackend.iam.domain.services.RoleValidationService;
import io.github.rafaviv.yakubackend.iam.interfaces.rest.resources.AuthenticationResponseResource;
import io.github.rafaviv.yakubackend.iam.interfaces.rest.resources.ChangePasswordResource;
import io.github.rafaviv.yakubackend.iam.interfaces.rest.resources.SignInResource;
import io.github.rafaviv.yakubackend.iam.interfaces.rest.resources.SignUpResource;
import io.github.rafaviv.yakubackend.iam.interfaces.rest.resources.UserResource;
import io.github.rafaviv.yakubackend.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import io.github.rafaviv.yakubackend.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import io.github.rafaviv.yakubackend.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Users REST Controller
 * <p>
 * This controller handles HTTP requests for user-related operations including
 * registration, authentication, and user management following REST principles.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/users")
public class UsersController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersController.class);

    private final UserCommandServiceImpl userCommandService;
    private final UserQueryServiceImpl userQueryService;
    private final RoleValidationService roleValidationService;

    public UsersController(
            UserCommandServiceImpl userCommandService,
            UserQueryServiceImpl userQueryService,
            RoleValidationService roleValidationService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.roleValidationService = roleValidationService;
    }

    /**
     * Register a new user
     * 
     * @param signUpResource the user registration data
     * @return ResponseEntity with success message
     */
    /**
     * Register a new user
     * 
     * @param signUpResource the user registration data
     * @return ResponseEntity with success message
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@Valid @RequestBody SignUpResource signUpResource) {
        try {
            LOGGER.info("Processing signup request for username: {}", signUpResource.username());

            SignUpCommand command = SignUpCommandFromResourceAssembler.toCommandFromResource(signUpResource);
            userCommandService.handle(command);

            LOGGER.info("User registered successfully: {}", signUpResource.username());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("User registered successfully");

        } catch (UserAlreadyExistsException e) {
            LOGGER.warn("Signup failed for username {}: {}", signUpResource.username(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Signup failed for username {}: {}", signUpResource.username(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error during signup for username {}: {}", signUpResource.username(),
                    e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred during registration");
        }
    }

    /**
     * Authenticate a user and return JWT token
     * 
     * @param signInResource the user authentication data
     * @return ResponseEntity with JWT token and user information
     */
    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInResource signInResource) {
        try {
            LOGGER.info("Processing signin request for username: {}", signInResource.username());

            SignInCommand command = SignInCommandFromResourceAssembler.toCommandFromResource(signInResource);
            userCommandService.handle(command);

            // Get user details for token generation
            Optional<User> userOptional = userQueryService
                    .handle(new GetUserByUsernameQuery(signInResource.username()));
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Authentication failed");
            }

            User user = userOptional.get();
            String token = userCommandService.generateTokenForUser(user);
            UserResource userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user);

            // Token expires in 7 days (604800 seconds)
            AuthenticationResponseResource response = AuthenticationResponseResource.of(token, 604800L, userResource);

            LOGGER.info("User authenticated successfully: {}", signInResource.username());
            return ResponseEntity.ok(response);

        } catch (InvalidCredentialsException e) {
            LOGGER.warn("Signin failed for username {}: {}", signInResource.username(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        } catch (UserAccountDeactivatedException e) {
            LOGGER.warn("Signin failed for username {}: {}", signInResource.username(), e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Signin failed for username {}: {}", signInResource.username(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error during signin for username {}: {}", signInResource.username(),
                    e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred during authentication");
        }
    }

    /**
     * Get user by username
     * 
     * @param username the user username
     * @return ResponseEntity with user information
     */
    @GetMapping("/by-username")
    public ResponseEntity<?> getUserByUsername(@RequestParam String username) {
        try {
            LOGGER.debug("Processing getUserByUsername request for username: {}", username);

            Optional<User> userOptional = userQueryService.handle(new GetUserByUsernameQuery(username));

            if (userOptional.isEmpty()) {
                throw new UserNotFoundException(username);
            }

            User user = userOptional.get();
            UserResource userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user);

            return ResponseEntity.ok(userResource);

        } catch (UserNotFoundException e) {
            LOGGER.warn("User not found with username: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error retrieving user by username {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while retrieving user");
        }
    }

    /**
     * Get user by ID
     *
     * @param id the user ID
     * @return ResponseEntity with user information
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            LOGGER.debug("Processing getUserById request for ID: {}", id);

            Optional<User> userOptional = userQueryService.handle(new GetUserByIdQuery(id));

            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found with id: " + id);
            }

            User user = userOptional.get();
            UserResource userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user);

            return ResponseEntity.ok(userResource);

        } catch (Exception e) {
            LOGGER.error("Unexpected error retrieving user by id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while retrieving user");
        }
    }

    /**
     * Get all users
     * 
     * @param farmId optional farm ID to filter users
     * @return ResponseEntity with list of all users
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers(@RequestParam(required = false) Long farmId) {
        try {
            LOGGER.debug("Processing getAllUsers request. farmId: {}", farmId);

            List<User> users;
            if (farmId != null) {
                users = userQueryService.handle(new io.github.rafaviv.yakubackend.iam.domain.model.queries.GetUsersByFarmIdQuery(farmId));
            } else {
                users = userQueryService.handle(new io.github.rafaviv.yakubackend.iam.domain.model.queries.GetAllUsersQuery());
            }
            
            List<UserResource> userResources = users.stream()
                    .map(UserResourceFromEntityAssembler::toResourceFromEntity)
                    .toList();

            return ResponseEntity.ok(userResources);

        } catch (Exception e) {
            LOGGER.error("Unexpected error retrieving all users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while retrieving users");
        }
    }

    /**
     * Get available roles for registration
     * 
     * @return ResponseEntity with list of available roles
     */
    @GetMapping("/available-roles")
    public ResponseEntity<?> getAvailableRoles() {
        try {
            LOGGER.debug("Processing getAvailableRoles request");

            var availableRoles = roleValidationService.getAvailableRolesForRegistration();

            return ResponseEntity.ok(availableRoles);

        } catch (Exception e) {
            LOGGER.error("Unexpected error retrieving available roles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while retrieving available roles");
        }
    }

    /**
     * Change user password
     *
     * @param id the user ID
     * @param resource the change password request
     * @return ResponseEntity with success or error
     */
    @PatchMapping("/{id}/password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody ChangePasswordResource resource) {
        try {
            userCommandService.changePassword(id, resource.currentPassword(), resource.newPassword());
            return ResponseEntity.ok().build();
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Current password incorrect");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error changing password");
        }
    }
}
