package com.proyecto.fundaciondeportiva.model.entity;

import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad 'perfiles_alumno'.
 * Esta es la NUEVA entidad, basada en tu diagrama.
 * Reemplaza tu 'PerfilAlumno.java' antiguo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "perfiles_alumno")
public class PerfilAlumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_estudiante", length = 20)
    private String codigoEstudiante;

    @Column(length = 15)
    private String dni;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelAcademico nivel;

    @Column(length = 20, nullable = false)
    private String grado;

    // Relación inversa 1:1 (opcional, pero útil)
    @OneToOne(mappedBy = "perfilAlumno")
    private Usuario usuario;
}