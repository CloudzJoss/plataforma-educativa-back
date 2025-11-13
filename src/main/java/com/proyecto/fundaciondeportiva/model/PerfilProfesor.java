package com.proyecto.fundaciondeportiva.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter@Setter
@Entity
@Table(name = "perfiles_profesor")
public class PerfilProfesor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // AÑADIDO: DNI (debe ser único)
    @Column(nullable = false, unique = true)
    private String dni;

    // ELIMINADO: departamento
    // private String departamento;
}