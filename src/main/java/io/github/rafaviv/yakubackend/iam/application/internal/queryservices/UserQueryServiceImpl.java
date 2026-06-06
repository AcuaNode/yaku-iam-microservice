package io.github.rafaviv.yakubackend.iam.application.internal.queryservices;

import io.github.rafaviv.yakubackend.iam.domain.model.aggregates.User;
import io.github.rafaviv.yakubackend.iam.domain.model.queries.GetAllUsersQuery;
import io.github.rafaviv.yakubackend.iam.domain.model.queries.GetUserByUsernameQuery;
import io.github.rafaviv.yakubackend.iam.domain.services.UserQueryService;
import io.github.rafaviv.yakubackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import io.github.rafaviv.yakubackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * User Query Service Implementation
 * <p>
 * This service handles query-based (read-only) operations for the User aggregate.
 * It implements the UserQueryService interface and provides business logic
 * for user retrieval operations.
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserQueryServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserQueryServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<User> handle(GetUserByUsernameQuery query) {
        LOGGER.debug("Processing GetUserByUsernameQuery for username: {}", query.username());
        
        Optional<User> user = userRepository.findByUsername(query.username());
        
        if (user.isPresent()) {
            LOGGER.debug("User found with ID: {}", user.get().getId());
        } else {
            LOGGER.debug("No user found with username: {}", query.username());
        }
        
        return user;
    }

    @Override
    public List<User> handle(GetAllUsersQuery query) {
        LOGGER.debug("Processing GetAllUsersQuery");
        
        List<User> users = userRepository.findAll();
        
        LOGGER.debug("Retrieved {} users", users.size());
        
        return users;
    }

    @Override
    public List<User> handle(io.github.rafaviv.yakubackend.iam.domain.model.queries.GetUsersByFarmIdQuery query) {
        LOGGER.debug("Processing GetUsersByFarmIdQuery for farm ID: {}", query.farmId());
        
        List<User> users = userRepository.findAllByAssignedFarmId(query.farmId());
        
        LOGGER.debug("Retrieved {} users for farm ID: {}", users.size(), query.farmId());
        
        return users;
    }
}
