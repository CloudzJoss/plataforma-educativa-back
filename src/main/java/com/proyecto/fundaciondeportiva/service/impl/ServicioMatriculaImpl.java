package com.proyecto.fundaciondeportiva.service.impl;

import com.proyecto.fundaciondeportiva.dto.request.MatriculaRequestDTO;
import com.proyecto.fundaciondeportiva.dto.response.MatriculaResponseDTO;
import com.proyecto.fundaciondeportiva.exception.RecursoNoEncontradoException;
import com.proyecto.fundaciondeportiva.exception.ValidacionException;
import com.proyecto.fundaciondeportiva.model.entity.Horario;
import com.proyecto.fundaciondeportiva.model.entity.Matricula;
import com.proyecto.fundaciondeportiva.model.entity.Seccion;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.EstadoMatricula;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import com.proyecto.fundaciondeportiva.repository.HorarioRepository; // ✅ IMPORTANTE
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    @Autowired
    private HorarioRepository horarioRepository; // ✅ INYECCIÓN PARA VALIDAR CRUCES

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
                throw new ValidacionException("La sección no está activa.");
            }

            // 3. Validar que la sección no haya finalizado
            if (seccion.getFechaFin().isBefore(LocalDate.now())) {
                throw new ValidacionException("La sección ya ha finalizado.");
            }

            // 4. Validar que el alumno no esté ya matriculado en ESTA sección específica
            if (matriculaRepository.existsByAlumnoIdAndSeccionId(alumnoId, request.getSeccionId())) {
                throw new ValidacionException("Ya estás matriculado en esta sección");
            }

            // --- 5. 🔒 VALIDACIÓN: NO REPETIR CURSO ---
            // Verificamos si el alumno ya tiene una matrícula activa para ESTE CURSO (en cualquier sección)
            Long cursoId = seccion.getCurso().getId();
            boolean yaTieneCurso = matriculaRepository.existeMatriculaActivaEnCurso(alumnoId, cursoId);

            if (yaTieneCurso) {
                throw new ValidacionException("Ya estás matriculado en una sección del curso '" + seccion.getCurso().getTitulo() + "'. No puedes inscribirte dos veces en el mismo curso.");
            }
            // ----------------------------------------------

            // 6. Validar que haya cupo disponible
            long matriculasActivas = matriculaRepository.countMatriculasActivasBySeccionId(request.getSeccionId());
            if (matriculasActivas >= seccion.getCapacidad()) {
                throw new ValidacionException("La sección ha alcanzado su capacidad máxima.");
            }

            // --- 7. VALIDACIÓN ESTRICTA DE NIVEL Y GRADO ---

            // 7.1 Validar Nivel
            if (!alumno.getPerfilAlumno().getNivel().equals(seccion.getNivelSeccion())) {
                throw new ValidacionException(
                        String.format("Nivel incorrecto. Tú eres de %s y la sección es de %s",
                                alumno.getPerfilAlumno().getNivel(), seccion.getNivelSeccion())
                );
            }

            // 7.2 Validar Grado (Extrayendo solo el número para comparar)
            Integer gradoAlumno = extraerNumeroGrado(alumno.getPerfilAlumno().getGrado());
            Integer gradoSeccion = extraerNumeroGrado(seccion.getGradoSeccion());

            if (gradoAlumno != null && gradoSeccion != null) {
                if (!gradoAlumno.equals(gradoSeccion)) {
                    throw new ValidacionException(
                            String.format("Grado incorrecto. Tú estás en %sº grado y la sección es para %sº grado.",
                                    gradoAlumno, gradoSeccion)
                    );
                }
            } else {
                // Fallback: Comparación de texto estricta si falla la numérica
                if (!alumno.getPerfilAlumno().getGrado().equalsIgnoreCase(seccion.getGradoSeccion())) {
                    logger.warn("Comparación de grados por texto estricto: {} vs {}",
                            alumno.getPerfilAlumno().getGrado(), seccion.getGradoSeccion());
                }
            }

            // --- 8. 🔒 VALIDAR CRUCE DE HORARIOS (ALUMNO) ---
            // Obtenemos los horarios de la nueva sección
            List<Horario> horariosNuevos = seccion.getHorarios();

            // Obtenemos todos los horarios que ya tiene ocupados el alumno
            List<Horario> horariosOcupados = horarioRepository.findHorariosDeAlumno(alumnoId);

            for (Horario nuevo : horariosNuevos) {
                for (Horario ocupado : horariosOcupados) {
                    // 1. ¿Es el mismo día?
                    if (nuevo.getDiaSemana() == ocupado.getDiaSemana()) {
                        // 2. ¿Se solapan las horas?
                        // Lógica: (InicioNuevo < FinOcupado) Y (FinNuevo > InicioOcupado)
                        boolean solapa = nuevo.getHoraInicio().isBefore(ocupado.getHoraFin()) &&
                                nuevo.getHoraFin().isAfter(ocupado.getHoraInicio());

                        if (solapa) {
                            throw new ValidacionException(
                                    String.format("Conflicto de horario: El %s de %s a %s choca con tu curso de '%s'",
                                            nuevo.getDiaSemana(), nuevo.getHoraInicio(), nuevo.getHoraFin(),
                                            ocupado.getSeccion().getCurso().getTitulo())
                            );
                        }
                    }
                }
            }
            // ---------------------------------------------------

            // 9. Crear la matrícula
            Matricula nuevaMatricula = Matricula.builder()
                    .alumno(alumno)
                    .seccion(seccion)
                    .estado(EstadoMatricula.ACTIVA)
                    .observaciones(request.getObservaciones())
                    .build();

            Matricula matriculaGuardada = matriculaRepository.save(nuevaMatricula);
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
        Matricula matricula = matriculaRepository.findByAlumnoIdAndSeccionId(alumnoId, seccionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró matrícula activa"));

        if (matricula.getEstado() != EstadoMatricula.ACTIVA) throw new ValidacionException("Matrícula no activa");
        if (matricula.getSeccion().getFechaFin().isBefore(LocalDate.now())) throw new ValidacionException("Curso finalizado");

        matricula.setEstado(EstadoMatricula.RETIRADA);
        matricula.setFechaRetiro(LocalDateTime.now());
        return MatriculaResponseDTO.deEntidad(matriculaRepository.save(matricula));
    }

    // --- MÉTODO: ELIMINAR MATRÍCULA (Baja Definitiva) ---
    @Override
    @Transactional
    public void eliminarMatriculaEstudiante(Long alumnoId, Long seccionId) {
        logger.info("Alumno ID {} solicita eliminar su matrícula de sección ID {}", alumnoId, seccionId);

        // 1. Buscar la matrícula específica de ese alumno y esa sección
        Matricula matricula = matriculaRepository.findByAlumnoIdAndSeccionId(alumnoId, seccionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No tienes una matrícula registrada en esta sección"));

        // 2. Validaciones de negocio
        if (matricula.getCalificacionFinal() != null) {
            throw new ValidacionException("No puedes eliminar una matrícula que ya tiene calificación. Debes solicitar un retiro.");
        }

        // 3. Eliminar físicamente
        matriculaRepository.delete(matricula);
        logger.info("Matrícula eliminada físicamente.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponseDTO> listarMisMatriculas(Long alumnoId) {
        if (!usuarioRepository.existsById(alumnoId)) throw new RecursoNoEncontradoException("Alumno no encontrado");
        return matriculaRepository.findByAlumnoId(alumnoId).stream().map(MatriculaResponseDTO::deEntidad).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponseDTO> listarMisMatriculasActivas(Long alumnoId) {
        if (!usuarioRepository.existsById(alumnoId)) throw new RecursoNoEncontradoException("Alumno no encontrado");
        return matriculaRepository.findByAlumnoIdAndEstado(alumnoId, EstadoMatricula.ACTIVA).stream().map(MatriculaResponseDTO::deEntidad).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponseDTO> listarAlumnosDeSeccion(Long seccionId) {
        if (!seccionRepository.existsById(seccionId)) throw new RecursoNoEncontradoException("Sección no encontrada");
        return matriculaRepository.findBySeccionId(seccionId).stream().map(MatriculaResponseDTO::deEntidad).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponseDTO> listarAlumnosActivosDeSeccion(Long seccionId) {
        if (!seccionRepository.existsById(seccionId)) throw new RecursoNoEncontradoException("Sección no encontrada");
        return matriculaRepository.findBySeccionIdAndEstado(seccionId, EstadoMatricula.ACTIVA).stream().map(MatriculaResponseDTO::deEntidad).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponseDTO> listarTodasLasMatriculas() {
        return matriculaRepository.findAll().stream().map(MatriculaResponseDTO::deEntidad).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MatriculaResponseDTO obtenerMatriculaPorId(Long id) {
        return MatriculaResponseDTO.deEntidad(matriculaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Matrícula no encontrada")));
    }

    @Override
    @Transactional
    public MatriculaResponseDTO actualizarEstadoMatricula(Long id, EstadoMatricula nuevoEstado) {
        Matricula matricula = matriculaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Matrícula no encontrada"));
        if (nuevoEstado == EstadoMatricula.RETIRADA && matricula.getFechaRetiro() == null) {
            matricula.setFechaRetiro(LocalDateTime.now());
        }
        matricula.setEstado(nuevoEstado);
        return MatriculaResponseDTO.deEntidad(matriculaRepository.save(matricula));
    }

    @Override
    @Transactional
    public MatriculaResponseDTO asignarCalificacion(Long id, Double calificacion) {
        if (calificacion < 0 || calificacion > 20) throw new ValidacionException("Calificación inválida");
        Matricula matricula = matriculaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Matrícula no encontrada"));
        matricula.setCalificacionFinal(calificacion);
        if (calificacion >= 11 && matricula.getSeccion().getFechaFin().isBefore(LocalDate.now())) {
            matricula.setEstado(EstadoMatricula.COMPLETADA);
        }
        return MatriculaResponseDTO.deEntidad(matriculaRepository.save(matricula));
    }

    @Override
    @Transactional
    public void eliminarMatricula(Long id) {
        if (!matriculaRepository.existsById(id)) throw new RecursoNoEncontradoException("Matrícula no encontrada");
        matriculaRepository.deleteById(id);
    }

    // --- MÉTODO AUXILIAR PARA EXTRAER EL NÚMERO DEL GRADO ---
    private Integer extraerNumeroGrado(String textoGrado) {
        if (textoGrado == null) return null;
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(textoGrado);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}