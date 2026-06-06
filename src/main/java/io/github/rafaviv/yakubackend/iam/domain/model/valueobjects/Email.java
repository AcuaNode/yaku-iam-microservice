package io.github.rafaviv.yakubackend.iam.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.regex.Pattern;

/**
 * Value object representing an email address.
 */
@Embeddable
public record Email(
        @NotBlank
        @jakarta.validation.constraints.Email
        @Size(max = 50)
        @Column(name = "email", length = 50)
        String address
) {
    public Email {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Email address cannot be null or blank");
        }
        if (!Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$").matcher(address).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    public Email() {
        this("unknown@unknown.com");
    }
}
