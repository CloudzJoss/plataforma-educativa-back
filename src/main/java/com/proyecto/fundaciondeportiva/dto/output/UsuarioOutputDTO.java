package com.proyecto.fundaciondeportiva.dto.output;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioOutputDTO {

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

    public static UsuarioOutputDTO deEntidad(Usuario usuario) {
        if (usuario == null) return null;

        UsuarioOutputDTO dto = UsuarioOutputDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .fechaCreacion(usuario.getFechaCreacion())
                .build();

        if (usuario.getPerfilAlumno() != null) {
            PerfilAlumno p = usuario.getPerfilAlumno();
            dto.setCodigoEstudiante(p.getCodigoEstudiante());
            dto.setDniAlumno(p.getDni());
            dto.setNivel(p.getNivel());
            dto.setGrado(p.getGrado());
        }

        if (usuario.getPerfilProfesor() != null) {
            PerfilProfesor p = usuario.getPerfilProfesor();
            dto.setDniProfesor(p.getDni());
            dto.setTelefono(p.getTelefono());
            dto.setExperiencia(p.getExperiencia());
            dto.setGradoAcademico(p.getGradoAcademico());
        }
        return dto;
    }
}