package com.proyecto.fundaciondeportiva.service.impl;

import com.proyecto.fundaciondeportiva.dto.request.RegistroAsistenciaDTO;
import com.proyecto.fundaciondeportiva.dto.response.AsistenciaDTO;
import com.proyecto.fundaciondeportiva.exception.RecursoNoEncontradoException;
import com.proyecto.fundaciondeportiva.model.entity.*;
import com.proyecto.fundaciondeportiva.model.enums.EstadoAsistencia;
import com.proyecto.fundaciondeportiva.model.enums.EstadoMatricula;
import com.proyecto.fundaciondeportiva.repository.*;
import com.proyecto.fundaciondeportiva.service.ServicioAsistencia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServicioAsistenciaImpl implements ServicioAsistencia {

    @Autowired
    private AsistenciaRepository asistenciaRepository;
    @Autowired
    private SesionRepository sesionRepository;
    @Autowired
    private MatriculaRepository matriculaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaDTO> obtenerAsistenciaDeSesion(Long sesionId) {
        Sesion sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesión no encontrada"));

        // 1. Obtener los alumnos matriculados
        List<Matricula> matriculas = matriculaRepository.findBySeccionIdAndEstado(sesion.getSeccion().getId(), EstadoMatricula.ACTIVA);

        // ✅ NUEVO: Ordenar la lista de matrículas por Apellido alfabéticamente
        matriculas.sort(Comparator.comparing(m -> m.getAlumno().getApellidos(), String.CASE_INSENSITIVE_ORDER));

        // 2. Obtener registros ya guardados
        List<Asistencia> asistenciasGuardadas = asistenciaRepository.findBySesionId(sesionId);

        Map<Long, Asistencia> mapaAsistencias = asistenciasGuardadas.stream()
                .collect(Collectors.toMap(a -> a.getAlumno().getId(), a -> a));

        // 3. Fusionar
        List<AsistenciaDTO> resultado = new ArrayList<>();

        for (Matricula matricula : matriculas) {
            Usuario alumno = matricula.getAlumno();
            Asistencia asistenciaExistente = mapaAsistencias.get(alumno.getId());

            // ✅ Formato: "Apellidos, Nombres" para que sea más fácil de leer en lista
            String nombreMostrar = alumno.getApellidos() + ", " + alumno.getNombres();

            AsistenciaDTO dto = AsistenciaDTO.builder()
                    .alumnoId(alumno.getId())
                    .nombreAlumno(nombreMostrar)
                    .codigoEstudiante(alumno.getPerfilAlumno() != null ? alumno.getPerfilAlumno().getCodigoEstudiante() : "S/C")
                    .build();

            if (asistenciaExistente != null) {
                dto.setAsistenciaId(asistenciaExistente.getId());
                dto.setEstado(asistenciaExistente.getEstado());
                dto.setObservacion(asistenciaExistente.getObservacion());
            } else {
                dto.setEstado(EstadoAsistencia.SIN_REGISTRAR);
                dto.setObservacion("");
            }
            resultado.add(dto);
        }

        return resultado;
    }

    @Override
    @Transactional
    public void registrarAsistenciaMasiva(RegistroAsistenciaDTO request) {
        Sesion sesion = sesionRepository.findById(request.getSesionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesión no encontrada"));

        for (RegistroAsistenciaDTO.DetalleAsistenciaDTO detalle : request.getDetalles()) {
            Usuario alumno = usuarioRepository.findById(detalle.getAlumnoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Alumno no encontrado ID: " + detalle.getAlumnoId()));

            // Buscamos si ya existe el registro para actualizarlo, sino creamos uno nuevo
            Asistencia asistencia = asistenciaRepository.findBySesionIdAndAlumnoId(sesion.getId(), alumno.getId())
                    .orElse(Asistencia.builder()
                            .sesion(sesion)
                            .alumno(alumno)
                            .build());

            asistencia.setEstado(detalle.getEstado());
            asistencia.setObservacion(detalle.getObservacion());

            asistenciaRepository.save(asistencia);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AsistenciaDTO obtenerMiAsistencia(Long sesionId, String emailAlumno) {
        Usuario alumno = usuarioRepository.findByEmail(emailAlumno)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Sesion sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesión no encontrada"));

        // Buscamos si existe registro
        return asistenciaRepository.findBySesionIdAndAlumnoId(sesionId, alumno.getId())
                .map(asistencia -> AsistenciaDTO.builder()
                        .asistenciaId(asistencia.getId())
                        .alumnoId(alumno.getId())
                        .nombreAlumno(alumno.getNombres() + " " + alumno.getApellidos())
                        .estado(asistencia.getEstado())
                        .observacion(asistencia.getObservacion())
                        .build())
                .orElse(AsistenciaDTO.builder()
                        .alumnoId(alumno.getId())
                        .estado(EstadoAsistencia.SIN_REGISTRAR) // Si no hay registro aún
                        .build());
    }
}