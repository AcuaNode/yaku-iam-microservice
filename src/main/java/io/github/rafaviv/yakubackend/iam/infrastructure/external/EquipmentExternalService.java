package io.github.rafaviv.yakubackend.iam.infrastructure.external;

import java.util.Optional;

public interface EquipmentExternalService {
    boolean isValidAndUnusedFarmToken(String token);
    Optional<Long> findFarmIdByToken(String token);
}