package com.proyecto.fundaciondeportiva.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatriculaRequestDTO {

    @NotNull(message = "El ID de la sección es obligatorio")
    private Long seccionId;

    //NUEVO: Opcional, solo se usa cuando el ADMIN registra la matrícula
    private Long alumnoId;

    private String observaciones;
}