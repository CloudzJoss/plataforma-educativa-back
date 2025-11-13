package com.proyecto.fundaciondeportiva.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter@Setter
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaDeCreacion = LocalDateTime.now();

    @OneToOne(cascade = CascadeType.ALL) // Si borras un usuario, se borra su perfil
    @JoinColumn(name = "perfil_alumno_id", referencedColumnName = "id")
    private PerfilAlumno perfilAlumno;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "perfil_profesor_id", referencedColumnName = "id")
    private PerfilProfesor perfilProfesor;
}