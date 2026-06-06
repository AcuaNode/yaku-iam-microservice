package io.github.rafaviv.yakubackend.iam.application.internal.eventhandlers;

import io.github.rafaviv.yakubackend.iam.domain.model.entities.Role;
import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.Roles;
import io.github.rafaviv.yakubackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application Ready Event Handler
 * <p>
 * This handler is responsible for initializing default data when the application starts.
 * It seeds the database with default roles if they don't already exist.
 * </p>
 */
@Component
public class ApplicationReadyEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationReadyEventHandler.class);

    private final RoleRepository roleRepository;

    public ApplicationReadyEventHandler(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Handles the ApplicationReadyEvent to seed default roles
     * @param event the ApplicationReadyEvent
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void handleApplicationReady(ApplicationReadyEvent event) {
        LOGGER.info("Application ready - seeding default roles");
        seedDefaultRoles();
    }

    /**
     * Seeds the database with default roles if they don't exist
     */
    private void seedDefaultRoles() {
        for (Roles roleEnum : Roles.values()) {
            if (!roleRepository.existsByName(roleEnum)) {
                Role role = new Role(roleEnum);
                roleRepository.save(role);
                LOGGER.info("Created default role: {}", roleEnum.name());
            } else {
                LOGGER.debug("Role already exists: {}", roleEnum.name());
            }
        }
        LOGGER.info("Default roles seeding completed");
    }
}
