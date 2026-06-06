package io.github.rafaviv.yakubackend.iam.domain.model.commands;

import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.Roles;

/**
 * Comando para registrar un nuevo usuario en el sistema.
 * Solo contiene información de autenticación (IAM): email, contraseña, nombres y apellidos.
 * Los datos del cliente (DNI, ingreso mensual, dirección, estado civil, teléfono) 
 * deben crearse por separado en el bounded context Cliente mediante POST /api/v1/clients.
 * 
 * @see com.firststudent.platform.viviendasmartbackend.client.domain.model.aggregates.Client
 */
public record SignUpCommand(
        String username,
        String email,
        String password,
        String firstName,
        String lastName,
        Roles requestedRole,
        String farmToken
) {
    public SignUpCommand {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty.");
        }
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        if (firstName == null || firstName.isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty.");
        }
        if (lastName == null || lastName.isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty.");
        }
        if (requestedRole == null) {
            throw new IllegalArgumentException("Requested role cannot be null.");
        }
    }
}
