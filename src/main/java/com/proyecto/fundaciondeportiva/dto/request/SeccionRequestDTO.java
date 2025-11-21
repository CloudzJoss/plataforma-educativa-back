package com.proyecto.fundaciondeportiva.dto.request;

import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import com.proyecto.fundaciondeportiva.model.enums.Turno;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para crear y actualizar secciones.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeccionRequestDTO {

    @NotBlank(message = "El nombre de la sección es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @NotNull(message = "El nivel de la sección es obligatorio")
    private NivelAcademico nivelSeccion;

    @NotBlank(message = "El grado de la sección es obligatorio")
    @Size(max = 20, message = "El grado no puede exceder 20 caracteres")
    private String gradoSeccion; // Ej: "5to A", "3ro B"

    @NotNull(message = "El turno es obligatorio")
    private Turno turno;

    @Size(max = 50, message = "El nombre del aula no puede exceder 50 caracteres")
    private String aula;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser al menos 1")
    @Max(value = 100, message = "La capacidad no puede exceder 100")
    private Integer capacidad;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    @NotNull(message = "El ID del curso es obligatorio")
    private Long cursoId;

    @NotBlank(message = "El DNI del profesor es obligatorio")
    @Size(min = 8, max = 15, message = "El DNI debe tener entre 8 y 15 caracteres")
    private String profesorDni;
}