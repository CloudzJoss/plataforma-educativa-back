package com.proyecto.fundaciondeportiva.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * DTO para crear o actualizar horarios de una sección.
 * Se utiliza dentro de SeccionRequestDTO para especificar los horarios.
 *
 * Ejemplo JSON:
 * {
 *   "diaSemana": "MONDAY",
 *   "horaInicio": "08:00",
 *   "horaFin": "09:30"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HorarioDTO {

    /**
     * Día de la semana (MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
     */
    @NotNull(message = "El día de la semana es obligatorio")
    private DayOfWeek diaSemana;

    /**
     * Hora de inicio de la clase en formato HH:mm (ej: 08:00)
     */
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    /**
     * Hora de fin de la clase en formato HH:mm (ej: 09:30)
     */
    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime horaFin;

    /**
     * Valida que la hora de inicio sea anterior a la de fin.
     * Esta validación se ejecuta después de que se instancia el objeto.
     *
     * @return true si horaInicio < horaFin
     */
    public boolean isHoraValida() {
        if (horaInicio == null || horaFin == null) {
            return false;
        }
        return horaInicio.isBefore(horaFin);
    }

    /**
     * Retorna una representación legible del horario.
     * Ej: "MONDAY 08:00 - 09:30"
     */
    public String getRepresentacion() {
        return String.format("%s %s - %s", diaSemana, horaInicio, horaFin);
    }
}