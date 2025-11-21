package com.proyecto.fundaciondeportiva.dto.response;

import com.proyecto.fundaciondeportiva.model.entity.Seccion;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import com.proyecto.fundaciondeportiva.model.enums.Turno;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeccionResponseDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private NivelAcademico nivelSeccion;
    private String gradoSeccion;
    private Turno turno;
    private String aula;
    private Integer capacidad;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean activa;
    private LocalDateTime fechaCreacion;

    // Información del curso
    private Long cursoId;
    private String codigoCurso;
    private String tituloCurso;
    private NivelAcademico nivelCurso;

    // Información del profesor
    private Long profesorId;
    private String nombreProfesor;
    private String dniProfesor;

    // Estadísticas
    private Integer estudiantesMatriculados;
    private Integer cuposDisponibles;
    private Boolean tieneCupo;
    private Boolean enPeriodoActivo;

    public static SeccionResponseDTO deEntidad(Seccion seccion) {
        if (seccion == null) {
            return null;
        }

        int estudiantesMatriculados = seccion.getNumeroEstudiantesMatriculados();
        int cuposDisponibles = seccion.getCapacidad() - estudiantesMatriculados;

        return SeccionResponseDTO.builder()
                .id(seccion.getId())
                .codigo(seccion.getCodigo())
                .nombre(seccion.getNombre())
                .nivelSeccion(seccion.getNivelSeccion())
                .gradoSeccion(seccion.getGradoSeccion())
                .turno(seccion.getTurno())
                .aula(seccion.getAula())
                .capacidad(seccion.getCapacidad())
                .fechaInicio(seccion.getFechaInicio())
                .fechaFin(seccion.getFechaFin())
                .activa(seccion.getActiva())
                .fechaCreacion(seccion.getFechaCreacion())
                // Curso
                .cursoId(seccion.getCurso().getId())
                .codigoCurso(seccion.getCurso().getCodigo())
                .tituloCurso(seccion.getCurso().getTitulo())
                .nivelCurso(seccion.getCurso().getNivelDestino())
                // Profesor
                .profesorId(seccion.getProfesor().getId())
                .nombreProfesor(seccion.getProfesor().getNombre())
                .dniProfesor(seccion.getProfesor().getPerfilProfesor() != null ?
                        seccion.getProfesor().getPerfilProfesor().getDni() : "N/A")
                // Estadísticas
                .estudiantesMatriculados(estudiantesMatriculados)
                .cuposDisponibles(cuposDisponibles)
                .tieneCupo(seccion.tieneCupoDisponible())
                .enPeriodoActivo(seccion.estaEnPeriodoActivo())
                .build();
    }
}