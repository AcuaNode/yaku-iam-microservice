package io.github.rafaviv.yakubackend.iam.domain.services;

import io.github.rafaviv.yakubackend.iam.domain.model.aggregates.FarmToken;
import io.github.rafaviv.yakubackend.iam.domain.model.commands.CreateFarmTokenCommand;

import java.util.Optional;

public interface FarmTokenCommandService {
    Optional<FarmToken> handle(CreateFarmTokenCommand command);
}