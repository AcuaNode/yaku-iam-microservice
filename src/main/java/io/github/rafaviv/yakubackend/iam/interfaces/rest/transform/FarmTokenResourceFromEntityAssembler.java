package io.github.rafaviv.yakubackend.iam.interfaces.rest.transform;

import io.github.rafaviv.yakubackend.iam.domain.model.aggregates.FarmToken;
import io.github.rafaviv.yakubackend.iam.interfaces.rest.resources.FarmTokenResource;

public class FarmTokenResourceFromEntityAssembler {
    public static FarmTokenResource toResourceFromEntity(FarmToken farmToken) {
        return new FarmTokenResource(
                farmToken.getId(),
                farmToken.getToken(),
                farmToken.getFarmId(),
                farmToken.isUsed()
        );
    }
}