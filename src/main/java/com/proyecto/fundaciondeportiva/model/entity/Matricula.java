package com.proyecto.fundaciondeportiva.model.entity;

import com.proyecto.fundaciondeportiva.model.enums.EstadoMatricula;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad 'matriculas'.
 * Representa la inscripción de un alumno en una sección específica.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "matriculas", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"alumno_id", "seccion_id"})
})
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Usuario alumno; // El alumno que se matricula

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seccion_id", nullable = false)
    private Seccion seccion; // La sección en la que se matricula

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoMatricula estado = EstadoMatricula.ACTIVA; // ACTIVA, RETIRADA, COMPLETADA

    @CreationTimestamp
    @Column(name = "fecha_matricula", updatable = false, nullable = false)
    private LocalDateTime fechaMatricula;

    @Column(name = "fecha_retiro")
    private LocalDateTime fechaRetiro;

    @Column(name = "calificacion_final")
    private Double calificacionFinal;

    @Column(length = 500)
    private String observaciones;
}