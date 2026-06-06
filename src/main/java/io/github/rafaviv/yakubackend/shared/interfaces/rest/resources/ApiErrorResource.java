package io.github.rafaviv.yakubackend.shared.interfaces.rest.resources;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Recurso estándar para respuestas de error en toda la API.
 */
public record ApiErrorResource(
        String message,
        int status,
        String error,
        String path,
        LocalDateTime timestamp,
        Map<String, String> details
) {
    public ApiErrorResource(String message, int status, String error, String path) {
        this(message, status, error, path, LocalDateTime.now(), null);
    }
}