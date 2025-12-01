package com.proyecto.fundaciondeportiva.dto.input;

import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioInputDTO {

    // CAMBIO: Separación de campos
    @NotBlank(message = "Los nombres son obligatorios")
    @Size(min = 2, max = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(min = 2, max = 100)
    private String apellidos;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;

    // --- Campos de PerfilProfesor ---
    @Size(min = 8, max = 15, message = "DNI de Profesor debe tener entre 8 y 15 caracteres")
    private String dniProfesor;
    private String telefono;
    private String experiencia;
    private String gradoAcademico;

    // --- Campos de PerfilAlumno ---
    @Size(min = 8, max = 15, message = "DNI de Alumno debe tener entre 8 y 15 caracteres")
    private String dniAlumno;
    private String codigoEstudiante;
    private NivelAcademico nivel;
    private String grado;
}