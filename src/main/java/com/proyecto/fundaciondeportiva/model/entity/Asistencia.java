package com.proyecto.fundaciondeportiva.model.entity;

import com.proyecto.fundaciondeportiva.model.enums.EstadoAsistencia;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "asistencias", uniqueConstraints = {
        // Un alumno solo puede tener un registro de asistencia por sesión
        @UniqueConstraint(columnNames = {"sesion_id", "alumno_id"})
})
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_id", nullable = false)
    @ToString.Exclude
    private Sesion sesion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    @ToString.Exclude
    private Usuario alumno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAsistencia estado;

    @Column(length = 255)
    private String observacion;
}