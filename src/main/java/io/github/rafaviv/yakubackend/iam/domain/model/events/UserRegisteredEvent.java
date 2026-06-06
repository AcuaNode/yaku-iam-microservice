package io.github.rafaviv.yakubackend.iam.domain.model.events;

public record UserRegisteredEvent(Long userId, String username, String email, String farmToken) {
}
