package com.proyecto.fundaciondeportiva.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.proyecto.fundaciondeportiva.model.enums.MomentoClase;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recursos")
public class Recurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo; // Ej: "Lectura Capítulo 1"

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private String url; // URL del archivo o Link externo

    @Column(length = 50)
    private String tipoArchivo; // PDF, VIDEO, LINK, IMAGEN

    // Clasificación del recurso
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MomentoClase momento; // ANTES, DURANTE, DESPUES

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private Sesion sesion;
}