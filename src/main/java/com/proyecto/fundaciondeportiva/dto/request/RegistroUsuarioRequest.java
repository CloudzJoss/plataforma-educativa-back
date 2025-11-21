package com.proyecto.fundaciondeportiva.dto.request;

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

/**
 * DTO para registrar un nuevo Usuario (Admin, Profesor o Alumno).
 * Este es el REEMPLAZO de tu 'UsuarioInputDTO' antiguo.
 * Contiene todos los campos del diagrama PlantUML.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroUsuarioRequest {

    // --- Campos de Usuario ---
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100)
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;

    // --- Campos de PerfilProfesor (Opcionales, solo para Rol.PROFESOR) ---
    @Size(min = 8, max = 15, message = "DNI de Profesor debe tener entre 8 y 15 caracteres")
    private String dniProfesor;

    private String telefono;
    private String experiencia;
    private String gradoAcademico;

    // --- Campos de PerfilAlumno (Opcionales, solo para Rol.ALUMNO) ---
    @Size(min = 8, max = 15, message = "DNI de Alumno debe tener entre 8 y 15 caracteres")
    private String dniAlumno;

    private String codigoEstudiante; // Opcional, se puede autogenerar

    // Estos son requeridos si el rol es ALUMNO
    private NivelAcademico nivel;
    private String grado;
}