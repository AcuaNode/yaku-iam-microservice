package io.github.rafaviv.yakubackend.iam.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

/**
 * Value object representing a hashed password.
 */
@Embeddable
public record HashedPassword(
        @NotBlank
        @Column(name = "password_hash")
        String hash
) {
    public HashedPassword {
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be null or blank");
        }
    }

    public HashedPassword() {
        this("empty");
    }
}
