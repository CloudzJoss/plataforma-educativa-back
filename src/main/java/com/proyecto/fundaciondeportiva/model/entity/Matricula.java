package com.proyecto.fundaciondeportiva.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.proyecto.fundaciondeportiva.model.enums.EstadoMatricula;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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
    @JsonIgnoreProperties({"matriculas", "asistencias", "seccionesAsignadas", "cursosCreados", "password"}) // 👈 AÑADIDO
    private Usuario alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seccion_id", nullable = false)
    @JsonBackReference("seccion-matriculas") // 👈 AÑADIDO - Lado hijo
    private Seccion seccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoMatricula estado = EstadoMatricula.ACTIVA;

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