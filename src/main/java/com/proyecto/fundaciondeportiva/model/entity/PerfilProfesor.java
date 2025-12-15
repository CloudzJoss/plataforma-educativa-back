package com.proyecto.fundaciondeportiva.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Lob
    private String experiencia;

    @Column(name = "grado_academico", length = 100)
    private String gradoAcademico;

    @OneToOne(mappedBy = "perfilProfesor")
    @JsonIgnore
    private Usuario usuario;
}