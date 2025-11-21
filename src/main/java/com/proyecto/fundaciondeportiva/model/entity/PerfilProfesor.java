package com.proyecto.fundaciondeportiva.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad 'perfiles_profesor'.
 * Esta es la NUEVA entidad, basada en tu diagrama.
 * Reemplaza tu 'PerfilProfesor.java' antiguo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "perfiles_profesor")
public class PerfilProfesor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 15)
    private String dni;

    @Column(length = 20)
    private String telefono;

    @Lob // Tipo de dato TEXT (largo)
    private String experiencia;

    @Column(name = "grado_academico", length = 100)
    private String gradoAcademico;

    // Relación inversa 1:1
    @OneToOne(mappedBy = "perfilProfesor")
    private Usuario usuario;
}