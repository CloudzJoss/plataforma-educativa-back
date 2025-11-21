package com.proyecto.fundaciondeportiva.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción personalizada para errores 400 (BAD_REQUEST).
 * La usaremos para "Email ya existe", "DNI ya existe", "Matrícula no válida", etc.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidacionException extends RuntimeException {
    public ValidacionException(String message) {
        super(message);
    }
}