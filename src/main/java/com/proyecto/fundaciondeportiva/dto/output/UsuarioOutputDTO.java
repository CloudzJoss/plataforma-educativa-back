package com.proyecto.fundaciondeportiva.dto.output;


import com.proyecto.fundaciondeportiva.model.Rol;
import lombok.Data;

@Data
public class UsuarioOutputDTO {
    private Long id;
    private String nombre;
    private String email;
    private Rol rol;

    // Campos aplanados de los perfiles
    private String carrera;
    private String codigoEstudiante;
    private String departamento;
}