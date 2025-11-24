package com.proyecto.fundaciondeportiva.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import com.proyecto.fundaciondeportiva.model.enums.Turno;
import com.proyecto.fundaciondeportiva.model.enums.EstadoMatricula; // Asegúrate de tener este enum
import jakarta.persistence.*;
import lombok.*; // Importar todo Lombok
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "secciones", uniqueConstraints = {
        @UniqueConstraint(columnNames = "codigo")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Seccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String codigo;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_seccion", nullable = false)
    private NivelAcademico nivelSeccion;

    @Column(name = "grado_seccion", length = 20, nullable = false)
    private String gradoSeccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Turno turno;

    @Column(length = 50)
    private String aula;

    @Column(nullable = false)
    @Builder.Default
    private Integer capacidad = 30;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false, nullable = false)
    private LocalDateTime fechaCreacion;

    // --- Relaciones (CON EXCLUDES PARA EVITAR RECURSIÓN) ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    @JsonIgnoreProperties({"secciones", "creadoPor"})
    @ToString.Exclude // 🚨 IMPORTANTE
    @EqualsAndHashCode.Exclude
    private Curso curso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesor_id", nullable = false)
    @JsonIgnoreProperties({"seccionesAsignadas", "matriculas", "asistencias", "cursosCreados", "password"})
    @ToString.Exclude // 🚨 IMPORTANTE
    @EqualsAndHashCode.Exclude
    private Usuario profesor;

    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference("seccion-matriculas")
    @ToString.Exclude // 🚨 IMPORTANTE: Rompe el ciclo infinito
    @EqualsAndHashCode.Exclude
    private Set<Matricula> matriculas = new HashSet<>();

    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnoreProperties("seccion")
    @ToString.Exclude // 🚨 IMPORTANTE
    @EqualsAndHashCode.Exclude
    private Set<Sesion> sesiones = new HashSet<>();

    // --- Métodos de utilidad ---

    public int getNumeroEstudiantesMatriculados() {
        if (matriculas == null) return 0;
        // Filtramos solo las matrículas activas
        return (int) matriculas.stream()
                .filter(m -> m.getEstado() == EstadoMatricula.ACTIVA)
                .count();
    }

    public boolean tieneCupoDisponible() {
        return getNumeroEstudiantesMatriculados() < capacidad;
    }

    public boolean estaEnPeriodoActivo() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(fechaInicio) && !hoy.isAfter(fechaFin);
    }
}