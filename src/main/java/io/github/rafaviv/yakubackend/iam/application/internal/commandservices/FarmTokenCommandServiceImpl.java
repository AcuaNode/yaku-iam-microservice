package io.github.rafaviv.yakubackend.iam.application.internal.commandservices;

import io.github.rafaviv.yakubackend.iam.domain.model.aggregates.FarmToken;
import io.github.rafaviv.yakubackend.iam.domain.model.commands.CreateFarmTokenCommand;
import io.github.rafaviv.yakubackend.iam.domain.services.FarmTokenCommandService;
import io.github.rafaviv.yakubackend.iam.infrastructure.persistence.jpa.repositories.FarmTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class FarmTokenCommandServiceImpl implements FarmTokenCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FarmTokenCommandServiceImpl.class);

    private final FarmTokenRepository farmTokenRepository;

    public FarmTokenCommandServiceImpl(FarmTokenRepository farmTokenRepository) {
        this.farmTokenRepository = farmTokenRepository;
    }

    @Override
    public Optional<FarmToken> handle(CreateFarmTokenCommand command) {
        LOGGER.info("Creating farm token for farm ID: {}", command.farmId());
        FarmToken farmToken = new FarmToken(command.farmId());
        FarmToken saved = farmTokenRepository.save(farmToken);
        LOGGER.info("Farm token created with token: {}", saved.getToken());
        return Optional.of(saved);
    }
}