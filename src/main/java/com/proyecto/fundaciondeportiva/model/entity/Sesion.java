package com.proyecto.fundaciondeportiva.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sesiones", indexes = {
        @Index(name = "idx_sesion_fecha", columnList = "fecha"),
        @Index(name = "idx_sesion_seccion", columnList = "seccion_id")
})
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Puede ser nulo al inicio, el profesor lo edita luego
    @Column(length = 200)
    private String tema;

    @Column(length = 500)
    private String descripcion; // Campo útil para instrucciones adicionales

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    // --- Relaciones ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seccion_id", nullable = false)
    @JsonIgnoreProperties({"sesiones", "matriculas", "horarios", "hibernateLazyInitializer"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Seccion seccion;

    @OneToMany(mappedBy = "sesion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Asistencia> asistencias = new HashSet<>();

    //  Relación para los archivos (Antes, Durante, Después)
    @OneToMany(mappedBy = "sesion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("sesion")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Recurso> recursos = new HashSet<>();
}