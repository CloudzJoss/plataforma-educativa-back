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

        // 1. Obtener los alumnos matriculados ACTIVOS en la sección
        List<Matricula> matriculas = matriculaRepository.findBySeccionIdAndEstado(sesion.getSeccion().getId(), EstadoMatricula.ACTIVA);

        // 2. Obtener registros de asistencia ya guardados en BD para esta sesión
        List<Asistencia> asistenciasGuardadas = asistenciaRepository.findBySesionId(sesionId);

        // Convertimos a un Map para búsqueda rápida por ID de alumno
        Map<Long, Asistencia> mapaAsistencias = asistenciasGuardadas.stream()
                .collect(Collectors.toMap(a -> a.getAlumno().getId(), a -> a));

        // 3. Fusionar listas: Iterar sobre matriculados y ver si tienen asistencia registrada
        List<AsistenciaDTO> resultado = new ArrayList<>();

        for (Matricula matricula : matriculas) {
            Usuario alumno = matricula.getAlumno();
            Asistencia asistenciaExistente = mapaAsistencias.get(alumno.getId());

            AsistenciaDTO dto = AsistenciaDTO.builder()
                    .alumnoId(alumno.getId())
                    .nombreAlumno(alumno.getNombres() + " " + alumno.getApellidos())
                    .codigoEstudiante(alumno.getPerfilAlumno() != null ? alumno.getPerfilAlumno().getCodigoEstudiante() : "S/C")
                    .build();

            if (asistenciaExistente != null) {
                // Ya se tomó lista previamente
                dto.setAsistenciaId(asistenciaExistente.getId());
                dto.setEstado(asistenciaExistente.getEstado());
                dto.setObservacion(asistenciaExistente.getObservacion());
            } else {
                // Aún no se toma lista, enviamos estado por defecto
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
}