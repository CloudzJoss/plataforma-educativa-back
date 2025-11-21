package com.proyecto.fundaciondeportiva.dto.response;

import com.proyecto.fundaciondeportiva.model.entity.Curso;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para devolver información de un Curso.
 * (Corregido SIN gradoDestino)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursoResponseDTO {

    private Long id;
    private String codigo;
    private String titulo;
    private String descripcion;
    private NivelAcademico nivelDestino;
    private LocalDateTime fechaCreacion;
    private String nombreCreador;
    private Long idCreador;

    public static CursoResponseDTO deEntidad(Curso curso) {
        if (curso == null) {
            return null;
        }

        String nombreCreador = (curso.getCreadoPor() != null) ? curso.getCreadoPor().getNombre() : "Sistema";
        Long idCreador = (curso.getCreadoPor() != null) ? curso.getCreadoPor().getId() : null;

        return CursoResponseDTO.builder()
                .id(curso.getId())
                .codigo(curso.getCodigo())
                .titulo(curso.getTitulo())
                .descripcion(curso.getDescripcion())
                .nivelDestino(curso.getNivelDestino())
                .fechaCreacion(curso.getFechaCreacion())
                .nombreCreador(nombreCreador)
                .idCreador(idCreador)
                .build();
    }
}