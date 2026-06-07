package io.github.rafaviv.yakubackend.iam.interfaces.rest.resources;

public record ChangePasswordResource(String currentPassword, String newPassword) {
}