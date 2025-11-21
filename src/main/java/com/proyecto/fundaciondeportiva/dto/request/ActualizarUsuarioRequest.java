// --- ARCHIVO: src/main/java/com/proyecto/fundaciondeportiva/dto/request/ActualizarUsuarioRequest.java ---
package com.proyecto.fundaciondeportiva.dto.request;

import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar un Usuario.
 * Este es el REEMPLAZO de tu 'UsuarioUpdateDTO' antiguo.
 * Todos los campos son opcionales y coinciden con el nuevo modelo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarUsuarioRequest {

    // --- Campos de Usuario ---
    @Size(min = 3, max = 100)
    private String nombre;

    @Email(message = "El formato del email no es válido")
    private String email;

    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password; // Opcional: solo si se quiere cambiar

    // --- Campos de PerfilProfesor ---
    @Size(min = 8, max = 15)
    private String dniProfesor;
    private String telefono;
    private String experiencia;
    private String gradoAcademico;

    // --- Campos de PerfilAlumno ---
    @Size(min = 8, max = 15)
    private String dniAlumno;
    private String codigoEstudiante;
    private NivelAcademico nivel;
    private String grado;
}