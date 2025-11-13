package com.proyecto.fundaciondeportiva.dto.output;


import com.proyecto.fundaciondeportiva.model.Rol;
import lombok.Data;

@Data
public class UsuarioOutputDTO {
    private Long id;
    private String nombre;
    private String email;
    private Rol rol;

    // AÑADIDO: DNI
    private String dni;

    // CAMPO DE ALUMNO: 'grado' en lugar de 'carrera'
    private String grado;
    private String codigoEstudiante;

    // ELIMINADO: private String departamento;
    // ELIMINADO: private String carrera;
}