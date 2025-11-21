package com.proyecto.fundaciondeportiva.dto.response;

import com.proyecto.fundaciondeportiva.model.entity.PerfilAlumno;
import com.proyecto.fundaciondeportiva.model.entity.PerfilProfesor;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para mostrar la información completa de un usuario.
 * REEMPLAZA a 'UsuarioOutputDTO' antiguo.
 * Este DTO "aplana" la información de Usuario y su Perfil asociado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {

    // Campos de Usuario
    private Long id;
    private String nombre;
    private String email;
    private Rol rol;
    private LocalDateTime fechaCreacion;

    // Campos de PerfilAlumno
    private String codigoEstudiante;
    private String dniAlumno;
    private NivelAcademico nivel;
    private String grado;

    // Campos de PerfilProfesor
    private String dniProfesor;
    private String telefono;
    private String experiencia;
    private String gradoAcademico;

    /**
     * Método de fábrica (static factory method) para convertir fácilmente
     * una Entidad Usuario (de la BD) en este DTO (para el Frontend).
     */
    public static UsuarioResponse deEntidad(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        UsuarioResponse dto = UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .fechaCreacion(usuario.getFechaCreacion())
                .build();

        if (usuario.getPerfilAlumno() != null) {
            PerfilAlumno perfil = usuario.getPerfilAlumno();
            dto.setCodigoEstudiante(perfil.getCodigoEstudiante());
            dto.setDniAlumno(perfil.getDni());
            dto.setNivel(perfil.getNivel());
            dto.setGrado(perfil.getGrado());
        }

        if (usuario.getPerfilProfesor() != null) {
            PerfilProfesor perfil = usuario.getPerfilProfesor();
            dto.setDniProfesor(perfil.getDni());
            dto.setTelefono(perfil.getTelefono());
            dto.setExperiencia(perfil.getExperiencia());
            dto.setGradoAcademico(perfil.getGradoAcademico());
        }

        return dto;
    }
}