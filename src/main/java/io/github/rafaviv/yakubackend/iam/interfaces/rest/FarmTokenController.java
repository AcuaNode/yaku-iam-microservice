package io.github.rafaviv.yakubackend.iam.interfaces.rest;

import io.github.rafaviv.yakubackend.iam.domain.model.commands.CreateFarmTokenCommand;
import io.github.rafaviv.yakubackend.iam.domain.services.FarmTokenCommandService;
import io.github.rafaviv.yakubackend.iam.interfaces.rest.resources.FarmTokenResource;
import io.github.rafaviv.yakubackend.iam.interfaces.rest.transform.FarmTokenResourceFromEntityAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/iam/farms/{farmId}/tokens")
public class FarmTokenController {

    private final FarmTokenCommandService farmTokenCommandService;

    public FarmTokenController(FarmTokenCommandService farmTokenCommandService) {
        this.farmTokenCommandService = farmTokenCommandService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FarmTokenResource> createFarmToken(@PathVariable Long farmId) {
        CreateFarmTokenCommand command = new CreateFarmTokenCommand(farmId);
        var farmToken = farmTokenCommandService.handle(command);
        if (farmToken.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var resource = FarmTokenResourceFromEntityAssembler.toResourceFromEntity(farmToken.get());
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }
}