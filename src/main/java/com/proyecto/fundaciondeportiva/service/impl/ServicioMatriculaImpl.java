package com.proyecto.fundaciondeportiva.service.impl;

import com.proyecto.fundaciondeportiva.dto.request.MatriculaRequestDTO;
import com.proyecto.fundaciondeportiva.dto.response.MatriculaResponseDTO;
import com.proyecto.fundaciondeportiva.exception.RecursoNoEncontradoException;
import com.proyecto.fundaciondeportiva.exception.ValidacionException;
import com.proyecto.fundaciondeportiva.model.entity.Matricula;
import com.proyecto.fundaciondeportiva.model.entity.Seccion;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.EstadoMatricula;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import com.proyecto.fundaciondeportiva.repository.MatriculaRepository;
import com.proyecto.fundaciondeportiva.repository.SeccionRepository;
import com.proyecto.fundaciondeportiva.repository.UsuarioRepository;
import com.proyecto.fundaciondeportiva.service.ServicioMatricula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioMatriculaImpl implements ServicioMatricula {

    private static final Logger logger = LoggerFactory.getLogger(ServicioMatriculaImpl.class);

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private SeccionRepository seccionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // --- OPERACIONES DE ALUMNO ---

    @Override
    @Transactional
    public MatriculaResponseDTO matricularseEnSeccion(Long alumnoId, MatriculaRequestDTO request) {
        logger.info("Alumno ID {} solicita matricularse en sección ID {}", alumnoId, request.getSeccionId());

        try {
            // 1. Validar que el usuario existe y es alumno
            Usuario alumno = usuarioRepository.findById(alumnoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Alumno no encontrado con ID: " + alumnoId));

            if (alumno.getRol() != Rol.ALUMNO) {
                throw new ValidacionException("El usuario no es un alumno");
            }

            if (alumno.getPerfilAlumno() == null) {
                throw new ValidacionException("El alumno no tiene un perfil de estudiante asociado");
            }

            // 2. Validar que la sección existe y está activa
            Seccion seccion = seccionRepository.findById(request.getSeccionId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada con ID: " + request.getSeccionId()));

            if (!seccion.getActiva()) {
                throw new ValidacionException("La sección no está activa. No se pueden aceptar nuevas matrículas.");
            }

            // 3. Validar que la sección no haya finalizado
            if (seccion.getFechaFin().isBefore(LocalDate.now())) {
                throw new ValidacionException("La sección ya ha finalizado. No se pueden aceptar nuevas matrículas.");
            }

            // 4. Validar que el alumno no esté ya matriculado
            if (matriculaRepository.existsByAlumnoIdAndSeccionId(alumnoId, request.getSeccionId())) {
                throw new ValidacionException("Ya estás matriculado en esta sección");
            }

            // 5. Validar que haya cupo disponible
            long matriculasActivas = matriculaRepository.countMatriculasActivasBySeccionId(request.getSeccionId());
            if (matriculasActivas >= seccion.getCapacidad()) {
                throw new ValidacionException("La sección ha alcanzado su capacidad máxima. No hay cupos disponibles.");
            }

            // 6. Validar que el nivel del alumno coincida con el nivel de la sección
            if (!alumno.getPerfilAlumno().getNivel().equals(seccion.getNivelSeccion())) {
                throw new ValidacionException(
                        String.format("No puedes matricularte en esta sección. Tu nivel es %s y la sección es para %s",
                                alumno.getPerfilAlumno().getNivel(), seccion.getNivelSeccion())
                );
            }

            // 7. Crear la matrícula
            Matricula nuevaMatricula = Matricula.builder()
                    .alumno(alumno)
                    .seccion(seccion)
                    .estado(EstadoMatricula.ACTIVA)
                    .observaciones(request.getObservaciones())
                    .build();

            Matricula matriculaGuardada = matriculaRepository.save(nuevaMatricula);
            logger.info("Matrícula creada exitosamente con ID: {}", matriculaGuardada.getId());

            return MatriculaResponseDTO.deEntidad(matriculaGuardada);

        } catch (RecursoNoEncontradoException | ValidacionException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al crear matrícula", e);
            throw new RuntimeException("Error al procesar la matrícula: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public MatriculaResponseDTO retirarseDeSeccion(Long alumnoId, Long seccionId) {
        logger.info("Alumno ID {} solicita retirarse de sección ID {}", alumnoId, seccionId);

        try {
            // Buscar la matrícula
            Matricula matricula = matriculaRepository.findByAlumnoIdAndSeccionId(alumnoId, seccionId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró una matrícula activa en esta sección"));

            // Validar que la matrícula esté activa
            if (matricula.getEstado() != EstadoMatricula.ACTIVA) {
                throw new ValidacionException("Esta matrícula ya no está activa");
            }

            // Validar que la sección no haya finalizado
            if (matricula.getSeccion().getFechaFin().isBefore(LocalDate.now())) {
                throw new ValidacionException("No puedes retirarte de una sección que ya finalizó");
            }

            // Actualizar el estado
            matricula.setEstado(EstadoMatricula.RETIRADA);
            matricula.setFechaRetiro(LocalDateTime.now());

            Matricula matriculaActualizada = matriculaRepository.save(matricula);
            logger.info("Alumno retirado exitosamente de la sección");

            return MatriculaResponseDTO.deEntidad(matriculaActualizada);

        } catch (RecursoNoEncontradoException | ValidacionException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al retirarse de la sección", e);
            throw new RuntimeException("Error al procesar el retiro: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponseDTO> listarMisMatriculas(Long alumnoId) {
        logger.info("Listando todas las matrículas del alumno ID: {}", alumnoId);

        // Validar que el alumno existe
        if (!usuarioRepository.existsById(alumnoId)) {
            throw new RecursoNoEncontradoException("Alumno no encontrado con ID: " + alumnoId);
        }

        return matriculaRepository.findByAlumnoId(alumnoId)
                .stream()
                .map(MatriculaResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponseDTO> listarMisMatriculasActivas(Long alumnoId) {
        logger.info("Listando matrículas activas del alumno ID: {}", alumnoId);

        // Validar que el alumno existe
        if (!usuarioRepository.existsById(alumnoId)) {
            throw new RecursoNoEncontradoException("Alumno no encontrado con ID: " + alumnoId);
        }

        return matriculaRepository.findByAlumnoIdAndEstado(alumnoId, EstadoMatricula.ACTIVA)
                .stream()
                .map(MatriculaResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    // --- OPERACIONES DE PROFESOR ---

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponseDTO> listarAlumnosDeSeccion(Long seccionId) {
        logger.info("Listando todos los alumnos de la sección ID: {}", seccionId);

        // Validar que la sección existe
        if (!seccionRepository.existsById(seccionId)) {
            throw new RecursoNoEncontradoException("Sección no encontrada con ID: " + seccionId);
        }

        return matriculaRepository.findBySeccionId(seccionId)
                .stream()
                .map(MatriculaResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponseDTO> listarAlumnosActivosDeSeccion(Long seccionId) {
        logger.info("Listando alumnos activos de la sección ID: {}", seccionId);

        // Validar que la sección existe
        if (!seccionRepository.existsById(seccionId)) {
            throw new RecursoNoEncontradoException("Sección no encontrada con ID: " + seccionId);
        }

        return matriculaRepository.findBySeccionIdAndEstado(seccionId, EstadoMatricula.ACTIVA)
                .stream()
                .map(MatriculaResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    // --- OPERACIONES DE ADMINISTRADOR ---

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponseDTO> listarTodasLasMatriculas() {
        logger.info("Listando todas las matrículas del sistema");

        return matriculaRepository.findAll()
                .stream()
                .map(MatriculaResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MatriculaResponseDTO obtenerMatriculaPorId(Long id) {
        logger.info("Obteniendo matrícula con ID: {}", id);

        Matricula matricula = matriculaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Matrícula no encontrada con ID: " + id));

        return MatriculaResponseDTO.deEntidad(matricula);
    }

    @Override
    @Transactional
    public MatriculaResponseDTO actualizarEstadoMatricula(Long id, EstadoMatricula nuevoEstado) {
        logger.info("Actualizando estado de matrícula ID {} a {}", id, nuevoEstado);

        try {
            Matricula matricula = matriculaRepository.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Matrícula no encontrada con ID: " + id));

            // Si se marca como retirada, registrar la fecha
            if (nuevoEstado == EstadoMatricula.RETIRADA && matricula.getFechaRetiro() == null) {
                matricula.setFechaRetiro(LocalDateTime.now());
            }

            matricula.setEstado(nuevoEstado);
            Matricula matriculaActualizada = matriculaRepository.save(matricula);

            logger.info("Estado de matrícula actualizado exitosamente");
            return MatriculaResponseDTO.deEntidad(matriculaActualizada);

        } catch (RecursoNoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al actualizar estado de matrícula", e);
            throw new RuntimeException("Error al actualizar el estado: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public MatriculaResponseDTO asignarCalificacion(Long id, Double calificacion) {
        logger.info("Asignando calificación {} a matrícula ID {}", calificacion, id);

        try {
            // Validar rango de calificación
            if (calificacion < 0 || calificacion > 20) {
                throw new ValidacionException("La calificación debe estar entre 0 y 20");
            }

            Matricula matricula = matriculaRepository.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Matrícula no encontrada con ID: " + id));

            matricula.setCalificacionFinal(calificacion);

            // Si la calificación es aprobatoria y el curso terminó, marcar como completada
            if (calificacion >= 11 && matricula.getSeccion().getFechaFin().isBefore(LocalDate.now())) {
                matricula.setEstado(EstadoMatricula.COMPLETADA);
            }

            Matricula matriculaActualizada = matriculaRepository.save(matricula);

            logger.info("Calificación asignada exitosamente");
            return MatriculaResponseDTO.deEntidad(matriculaActualizada);

        } catch (RecursoNoEncontradoException | ValidacionException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al asignar calificación", e);
            throw new RuntimeException("Error al asignar calificación: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void eliminarMatricula(Long id) {
        logger.info("Eliminando matrícula con ID: {}", id);

        if (!matriculaRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Matrícula no encontrada con ID: " + id);
        }

        try {
            matriculaRepository.deleteById(id);
            logger.info("Matrícula eliminada exitosamente");
        } catch (Exception e) {
            logger.error("Error al eliminar matrícula", e);
            throw new RuntimeException("Error al eliminar la matrícula: " + e.getMessage(), e);
        }
    }
}