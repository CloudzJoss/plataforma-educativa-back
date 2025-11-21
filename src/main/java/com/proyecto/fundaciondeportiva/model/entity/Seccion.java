package com.proyecto.fundaciondeportiva.model.entity;

import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import com.proyecto.fundaciondeportiva.model.enums.Turno;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad 'secciones'.
 * Representa una instancia real de un curso en un periodo específico.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "secciones", uniqueConstraints = {
        @UniqueConstraint(columnNames = "codigo")
})
public class Seccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String codigo; // Ej: "SEC-001", "SEC-002"

    @Column(length = 100, nullable = false)
    private String nombre; // Ej: "Matemática - 5to A - Mañana"

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_seccion", nullable = false)
    private NivelAcademico nivelSeccion; // INICIAL, PRIMARIA, SECUNDARIA

    @Column(name = "grado_seccion", length = 20, nullable = false)
    private String gradoSeccion; // Ej: "5to A", "3ro B", "1ro C"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Turno turno; // MAÑANA, TARDE, NOCHE

    @Column(length = 50)
    private String aula; // Ej: "Aula 101", "Lab 3"

    @Column(nullable = false)
    @Builder.Default
    private Integer capacidad = 30; // Capacidad máxima de estudiantes

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = true; // Permite desactivar sin eliminar

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false, nullable = false)
    private LocalDateTime fechaCreacion;

    // --- Relaciones ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesor_id", nullable = false)
    private Usuario profesor; // Debe tener rol PROFESOR

    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Matricula> matriculas = new HashSet<>();

    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Sesion> sesiones = new HashSet<>();

    // --- Métodos de utilidad ---

    /**
     * Retorna el número actual de estudiantes matriculados
     */
    public int getNumeroEstudiantesMatriculados() {
        return matriculas != null ? matriculas.size() : 0;
    }

    /**
     * Verifica si la sección tiene cupo disponible
     */
    public boolean tieneCupoDisponible() {
        return getNumeroEstudiantesMatriculados() < capacidad;
    }

    /**
     * Verifica si la sección está en el periodo activo
     */
    public boolean estaEnPeriodoActivo() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(fechaInicio) && !hoy.isAfter(fechaFin);
    }
}