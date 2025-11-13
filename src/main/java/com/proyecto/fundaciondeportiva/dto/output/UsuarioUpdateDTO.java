package com.proyecto.fundaciondeportiva.dto.output;

import com.proyecto.fundaciondeportiva.model.Rol;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UsuarioUpdateDTO {

    private String nombre;

    @Email(message = "Si se envía un email, debe tener formato válido")
    private String email;

    private String password;

    // AÑADIDO: DNI (opcional)
    private String dni;

    // CAMPO DE ALUMNO: 'grado' en lugar de 'carrera' (opcional)
    private String grado;

    // MANTENIDO: Código de estudiante (opcional)
    private String codigoEstudiante;

    // ELIMINADO: private String carrera;
    // ELIMINADO: private String departamento;
}