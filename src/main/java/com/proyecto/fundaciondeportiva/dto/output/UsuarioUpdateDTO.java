package com.proyecto.fundaciondeportiva.dto.input;

import com.proyecto.fundaciondeportiva.model.Rol;
import jakarta.validation.constraints.Email;
// Quitamos NotBlank y NotNull de la mayoría de los campos
import lombok.Data;

@Data
public class UsuarioUpdateDTO {

    // Opcionales: solo se actualizan si se envían
    private String nombre;

    @Email(message = "Si se envía un email, debe tener formato válido") // Mantenemos Email si se provee
    private String email;

    private String password; // Opcional: para cambiar contraseña

    // private Rol rol; // Generalmente no se permite cambiar el rol en una edición simple. Si lo necesitas, descomenta.

    // Campos de perfil también opcionales
    private String carrera;
    private String codigoEstudiante;
    private String departamento;
}
