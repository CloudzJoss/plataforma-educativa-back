package com.proyecto.fundaciondeportiva.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Entidad que representa un horario específico de una sección.
 * Cada sección puede tener múltiples horarios (lunes, miércoles, viernes, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "horarios", indexes = {
        @Index(name = "idx_seccion_dia", columnList = "seccion_id, dia_semana"),
        @Index(name = "idx_profesor_horario", columnList = "seccion_id, dia_semana, hora_inicio")
})
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Día de la semana (MONDAY, TUESDAY, WEDNESDAY, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private DayOfWeek diaSemana;

    /**
     * Hora de inicio del clase (ej: 08:00)
     */
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    /**
     * Hora de fin de la clase (ej: 09:30)
     */
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    /**
     * Relación con la sección a la que pertenece este horario
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seccion_id", nullable = false)
    @JsonBackReference("seccion-horarios")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Seccion seccion;

    /**
     * Valida que la hora de inicio sea anterior a la de fin
     * @return true si el horario es válido
     */
    @Transient
    public boolean esValido() {
        return horaInicio != null && horaFin != null && horaInicio.isBefore(horaFin);
    }

    /**
     * Verifica si este horario se solapa con otro en el mismo día
     * @param otro El otro horario a comparar
     * @return true si hay solapamiento
     */
    @Transient
    public boolean solapaConHorario(Horario otro) {
        if (otro == null || !this.diaSemana.equals(otro.diaSemana)) {
            return false;
        }
        // Lógica: (InicioEste < FinOtro) Y (FinEste > InicioOtro)
        return this.horaInicio.isBefore(otro.horaFin) &&
                this.horaFin.isAfter(otro.horaInicio);
    }

    /**
     * Retorna una representación legible del horario
     * Ej: "Lunes 08:00 - 09:30"
     */
    @Transient
    public String getRepresentacionLegible() {
        String nombreDia = diaSemana.toString();
        return String.format("%s %s - %s", nombreDia, horaInicio, horaFin);
    }
}