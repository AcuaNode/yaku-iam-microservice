package io.github.rafaviv.yakubackend.iam.interfaces.rest.transform;

import io.github.rafaviv.yakubackend.iam.domain.model.commands.SignInCommand;
import io.github.rafaviv.yakubackend.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {
    public static SignInCommand toCommandFromResource(SignInResource resource) {
        return new SignInCommand(
                resource.username(),
                resource.password()
        );
    }
}
