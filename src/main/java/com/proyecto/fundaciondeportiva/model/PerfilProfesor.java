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
    private String departamento;
}