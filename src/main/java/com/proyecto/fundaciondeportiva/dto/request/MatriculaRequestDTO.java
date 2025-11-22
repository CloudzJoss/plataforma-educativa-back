package com.proyecto.fundaciondeportiva.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear una matrícula.
 * Solo requiere el ID de la sección, ya que el alumno se identifica por JWT.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatriculaRequestDTO {

    @NotNull(message = "El ID de la sección es obligatorio")
    private Long seccionId;

    private String observaciones; // Opcional
}