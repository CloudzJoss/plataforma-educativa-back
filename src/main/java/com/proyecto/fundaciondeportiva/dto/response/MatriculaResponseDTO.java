package com.proyecto.fundaciondeportiva.dto.response;

import com.proyecto.fundaciondeportiva.dto.request.HorarioDTO; // Asegúrate de tener este DTO creado
import com.proyecto.fundaciondeportiva.model.entity.Matricula;
import com.proyecto.fundaciondeportiva.model.enums.EstadoMatricula;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
// import com.proyecto.fundaciondeportiva.model.enums.Turno; // ❌ Eliminado
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatriculaResponseDTO {

    private Long id;
    private EstadoMatricula estado;
    private LocalDateTime fechaMatricula;
    private LocalDateTime fechaRetiro;
    private Double calificacionFinal;
    private String observaciones;

    // Información del Alumno
    private Long alumnoId;
    private String nombreAlumno;
    private String dniAlumno;
    private String codigoEstudiante;
    private NivelAcademico nivelAlumno;
    private String gradoAlumno;

    // Información de la Sección
    private Long seccionId;
    private String codigoSeccion;
    private String nombreSeccion;

    // ❌ private Turno turnoSeccion; // Eliminado

    // ✅ NUEVO: Lista de horarios para mostrar en el frontend
    private List<HorarioDTO> horarios;

    private String aulaSeccion;
    private LocalDate fechaInicioSeccion;
    private LocalDate fechaFinSeccion;

    // Información del Curso
    private Long cursoId;
    private String codigoCurso;
    private String tituloCurso;
    private NivelAcademico nivelCurso;

    // Información del Profesor
    private Long profesorId;
    private String nombreProfesor;
    private String dniProfesor;

    public static MatriculaResponseDTO deEntidad(Matricula matricula) {
        if (matricula == null) {
            return null;
        }

        // Convertir la lista de horarios de la sección a DTOs
        List<HorarioDTO> listaHorarios = matricula.getSeccion().getHorarios().stream()
                .map(h -> new HorarioDTO(h.getDiaSemana(), h.getHoraInicio(), h.getHoraFin()))
                .collect(Collectors.toList());

        return MatriculaResponseDTO.builder()
                .id(matricula.getId())
                .estado(matricula.getEstado())
                .fechaMatricula(matricula.getFechaMatricula())
                .fechaRetiro(matricula.getFechaRetiro())
                .calificacionFinal(matricula.getCalificacionFinal())
                .observaciones(matricula.getObservaciones())
                // Alumno
                .alumnoId(matricula.getAlumno().getId())
                .nombreAlumno(matricula.getAlumno().getNombre())
                .dniAlumno(matricula.getAlumno().getPerfilAlumno() != null ?
                        matricula.getAlumno().getPerfilAlumno().getDni() : null)
                .codigoEstudiante(matricula.getAlumno().getPerfilAlumno() != null ?
                        matricula.getAlumno().getPerfilAlumno().getCodigoEstudiante() : null)
                .nivelAlumno(matricula.getAlumno().getPerfilAlumno() != null ?
                        matricula.getAlumno().getPerfilAlumno().getNivel() : null)
                .gradoAlumno(matricula.getAlumno().getPerfilAlumno() != null ?
                        matricula.getAlumno().getPerfilAlumno().getGrado() : null)
                // Sección
                .seccionId(matricula.getSeccion().getId())
                .codigoSeccion(matricula.getSeccion().getCodigo())
                .nombreSeccion(matricula.getSeccion().getNombre())
                //.turnoSeccion(matricula.getSeccion().getTurno()) // ❌ Eliminado
                .horarios(listaHorarios) // ✅ Agregado
                .aulaSeccion(matricula.getSeccion().getAula())
                .fechaInicioSeccion(matricula.getSeccion().getFechaInicio())
                .fechaFinSeccion(matricula.getSeccion().getFechaFin())
                // Curso
                .cursoId(matricula.getSeccion().getCurso().getId())
                .codigoCurso(matricula.getSeccion().getCurso().getCodigo())
                .tituloCurso(matricula.getSeccion().getCurso().getTitulo())
                .nivelCurso(matricula.getSeccion().getCurso().getNivelDestino())
                // Profesor
                .profesorId(matricula.getSeccion().getProfesor().getId())
                .nombreProfesor(matricula.getSeccion().getProfesor().getNombre())
                .dniProfesor(matricula.getSeccion().getProfesor().getPerfilProfesor() != null ?
                        matricula.getSeccion().getProfesor().getPerfilProfesor().getDni() : null)
                .build();
    }
}