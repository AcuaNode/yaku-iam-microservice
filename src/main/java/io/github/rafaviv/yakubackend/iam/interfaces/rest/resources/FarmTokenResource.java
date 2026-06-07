package io.github.rafaviv.yakubackend.iam.interfaces.rest.resources;

public record FarmTokenResource(Long id, String token, Long farmId, boolean isUsed) {
}