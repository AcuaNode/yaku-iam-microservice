package io.github.rafaviv.yakubackend.iam.application.internal.commandservices;

import io.github.rafaviv.yakubackend.iam.application.internal.outboundservices.hashing.HashingService;
import io.github.rafaviv.yakubackend.iam.application.internal.outboundservices.tokens.TokenService;
import io.github.rafaviv.yakubackend.iam.domain.model.aggregates.User;
import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.Email;
import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.HashedPassword;
import io.github.rafaviv.yakubackend.iam.domain.model.commands.SignInCommand;
import io.github.rafaviv.yakubackend.iam.domain.model.commands.SignUpCommand;
import io.github.rafaviv.yakubackend.iam.domain.model.entities.Role;
import io.github.rafaviv.yakubackend.iam.domain.model.exceptions.InvalidCredentialsException;
import io.github.rafaviv.yakubackend.iam.domain.model.exceptions.UserAccountDeactivatedException;
import io.github.rafaviv.yakubackend.iam.domain.model.exceptions.UserAlreadyExistsException;
import io.github.rafaviv.yakubackend.iam.domain.services.RoleValidationService;
import io.github.rafaviv.yakubackend.iam.domain.services.UserCommandService;
import io.github.rafaviv.yakubackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import io.github.rafaviv.yakubackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import io.github.rafaviv.yakubackend.iam.infrastructure.external.EquipmentExternalService; // Asegúrate de este import
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserCommandServiceImpl implements UserCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserCommandServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final RoleValidationService roleValidationService;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    private final EquipmentExternalService equipmentExternalService; // Tipo correcto

    public UserCommandServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            HashingService hashingService,
            TokenService tokenService,
            RoleValidationService roleValidationService,
            org.springframework.context.ApplicationEventPublisher eventPublisher,
            EquipmentExternalService equipmentExternalService) { // Parámetro con tipo correcto
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.roleValidationService = roleValidationService;
        this.eventPublisher = eventPublisher;
        this.equipmentExternalService = equipmentExternalService; // Asignación correcta
    }

    @Override
    public void handle(SignUpCommand command) {
        LOGGER.info("Processing SignUp command for username: {} with role: {}",
            command.username(), command.requestedRole());

        if (userRepository.existsByUsername(command.username())) {
            throw new UserAlreadyExistsException(command.username());
        }

        if (!roleValidationService.canRequestRole(command.requestedRole())) {
            throw new IllegalArgumentException("Cannot request role: " + command.requestedRole());
        }

        if (command.requestedRole() == io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.Roles.OPERATOR) {
            if (command.farmToken() == null || command.farmToken().isBlank()) {
                throw new IllegalArgumentException("Farm token is required for OPERATOR role");
            }
            if (!equipmentExternalService.isValidAndUnusedFarmToken(command.farmToken())) {
                throw new IllegalArgumentException("Invalid or already used Farm token");
            }
        }

        String hashedPassword = hashingService.encode(command.password());

        User user = new User(
                command.username(),
                new Email(command.email()),
                new HashedPassword(hashedPassword),
                command.firstName(),
                command.lastName(),
                false
        );

        Role requestedRole = roleRepository.findByName(command.requestedRole())
                .orElseThrow(() -> new IllegalStateException("Requested role " + command.requestedRole() + " not found"));
        
        user.addRole(requestedRole);
        
        if (command.requestedRole() == io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.Roles.OPERATOR) {
        	equipmentExternalService.findFarmIdByToken(command.farmToken()).ifPresent(user::setAssignedFarmId);
        }

        User savedUser = userRepository.save(user);
        LOGGER.info("User registered successfully with ID: {}", savedUser.getId());

        eventPublisher.publishEvent(new io.github.rafaviv.yakubackend.iam.domain.model.events.UserRegisteredEvent(
                savedUser.getId(), savedUser.getUsername(), savedUser.getEmail().address(), command.farmToken()));
    }

    @Override
    public void handle(SignInCommand command) {
        LOGGER.info("Processing SignIn command for username: {}", command.username());

        Optional<User> userOptional = userRepository.findByUsername(command.username());
        if (userOptional.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        User user = userOptional.get();

        if (!user.getActive()) {
            throw new UserAccountDeactivatedException(command.username());
        }

        if (!hashingService.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        LOGGER.info("User authenticated successfully with ID: {}", user.getId());
    }

    public String generateTokenForUser(User user) {
        String userRole = user.getRoles().isEmpty() ? "OPERATOR" : 
                         user.getRoles().get(0).getName().name();
        return tokenService.generateToken(user.getId(), userRole);
    }
}