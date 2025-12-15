package com.proyecto.fundaciondeportiva.model.entity;

import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entidad 'cursos'.
 * 🚨 ACTUALIZADA: Se ha eliminado 'gradoDestino'.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cursos", uniqueConstraints = {
        @UniqueConstraint(columnNames = "codigo")
})
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String codigo;

    @Column(length = 100, nullable = false)
    private String titulo;

    @Lob
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_destino", nullable = false)
    private NivelAcademico nivelDestino;


    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false, nullable = false)
    private LocalDateTime fechaCreacion;

    // --- Relaciones ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Seccion> secciones;
}