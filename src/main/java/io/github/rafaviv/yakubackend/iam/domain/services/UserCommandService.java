package io.github.rafaviv.yakubackend.iam.domain.services;

import io.github.rafaviv.yakubackend.iam.domain.model.commands.SignInCommand;
import io.github.rafaviv.yakubackend.iam.domain.model.commands.SignUpCommand;

public interface UserCommandService {
    void handle(SignUpCommand command);

    void handle(SignInCommand command);

    void changePassword(Long userId, String currentPassword, String newPassword);
}
