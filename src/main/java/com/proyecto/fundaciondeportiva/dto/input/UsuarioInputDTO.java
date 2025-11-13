package com.proyecto.fundaciondeportiva.dto.input;


import com.proyecto.fundaciondeportiva.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsuarioInputDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;

    // AÑADIDO: DNI (Común para Alumno y Profesor)
    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    // CAMPO DE ALUMNO: 'grado' en lugar de 'carrera'
    private String grado;

    // ELIMINADO: private String carrera;
    // ELIMINADO: private String codigoEstudiante; (Se genera en el servicio)
    // ELIMINADO: private String departamento;
}