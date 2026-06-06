package io.github.rafaviv.yakubackend.shared.interfaces.rest.resources.advice;

import io.github.rafaviv.yakubackend.shared.interfaces.rest.resources.ApiErrorResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResource> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        ApiErrorResource error = new ApiErrorResource(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResource> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {

        ApiErrorResource error = new ApiErrorResource(
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
}