package com.proyecto.fundaciondeportiva.dto.request;

import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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
    private String gradoSeccion;

    // ❌ ELIMINADO: private Turno turno;

    // ✅ AGREGADO: Lista de horarios
    @NotEmpty(message = "Debe asignar al menos un horario")
    private List<HorarioDTO> horarios;

    @Size(max = 50, message = "El nombre del aula no puede exceder 50 caracteres")
    private String aula;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1)
    private Integer capacidad;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    @NotNull(message = "El ID del curso es obligatorio")
    private Long cursoId;

    @NotBlank(message = "El DNI del profesor es obligatorio")
    private String profesorDni;
}