package com.proyecto.fundaciondeportiva.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter@Setter
@Entity
@Table(name = "perfiles_alumno")
public class PerfilAlumno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CAMBIO: Grado en lugar de Carrera
    private String grado;

    // AÑADIDO: DNI (debe ser único)
    @Column(nullable = false, unique = true)
    private String dni;

    // MANTENIDO: Código de Estudiante
    @Column(nullable = false, unique = true)
    private String codigoEstudiante;
}