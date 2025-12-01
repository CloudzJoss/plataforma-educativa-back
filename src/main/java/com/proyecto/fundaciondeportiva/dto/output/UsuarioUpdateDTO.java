package com.proyecto.fundaciondeportiva.dto.output;

import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateDTO {

    // CAMBIO: Separación de campos
    @Size(min = 2, max = 100)
    private String nombres;

    @Size(min = 2, max = 100)
    private String apellidos;

    @Email(message = "El formato del email no es válido")
    private String email;

    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    // --- Perfiles ---
    @Size(min = 8, max = 15)
    private String dniProfesor;
    private String telefono;
    private String experiencia;
    private String gradoAcademico;

    @Size(min = 8, max = 15)
    private String dniAlumno;
    private String codigoEstudiante;
    private NivelAcademico nivel;
    private String grado;
}