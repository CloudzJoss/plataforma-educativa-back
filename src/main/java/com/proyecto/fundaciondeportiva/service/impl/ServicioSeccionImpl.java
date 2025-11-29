package com.proyecto.fundaciondeportiva.service.impl;

import com.proyecto.fundaciondeportiva.dto.request.HorarioDTO;
import com.proyecto.fundaciondeportiva.dto.request.SeccionRequestDTO;
import com.proyecto.fundaciondeportiva.dto.response.SeccionResponseDTO;
import com.proyecto.fundaciondeportiva.exception.RecursoNoEncontradoException;
import com.proyecto.fundaciondeportiva.exception.ValidacionException;
import com.proyecto.fundaciondeportiva.model.entity.Curso;
import com.proyecto.fundaciondeportiva.model.entity.Horario;
import com.proyecto.fundaciondeportiva.model.entity.PerfilProfesor;
import com.proyecto.fundaciondeportiva.model.entity.Seccion;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import com.proyecto.fundaciondeportiva.repository.CursoRepository;
import com.proyecto.fundaciondeportiva.repository.HorarioRepository;
import com.proyecto.fundaciondeportiva.repository.PerfilProfesorRepository;
import com.proyecto.fundaciondeportiva.repository.SeccionRepository;
import com.proyecto.fundaciondeportiva.repository.UsuarioRepository;
import com.proyecto.fundaciondeportiva.service.ServicioSeccion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioSeccionImpl implements ServicioSeccion {

    private static final Logger logger = LoggerFactory.getLogger(ServicioSeccionImpl.class);

    @Autowired
    private SeccionRepository seccionRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilProfesorRepository perfilProfesorRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarTodasLasSecciones() {
        logger.info("Listando todas las secciones");
        return seccionRepository.findAll().stream()
                .map(SeccionResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarSeccionesActivas() {
        logger.info("Listando secciones activas");
        return seccionRepository.findByActivaTrue().stream()
                .map(SeccionResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SeccionResponseDTO obtenerSeccionPorId(Long id) {
        logger.info("Obteniendo sección ID: {}", id);
        Seccion seccion = seccionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada con id: " + id));
        return SeccionResponseDTO.deEntidad(seccion);
    }

    @Override
    @Transactional
    public SeccionResponseDTO crearSeccion(SeccionRequestDTO request) {
        logger.info("Creando nueva sección: {}", request.getNombre());

        // Validar fechas
        validarFechas(request.getFechaInicio(), request.getFechaFin());

        // Validar que el curso existe
        Curso curso = cursoRepository.findById(request.getCursoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado con id: " + request.getCursoId()));

        // Validar coincidencia de nivel
        if (!request.getNivelSeccion().equals(curso.getNivelDestino())) {
            throw new ValidacionException(
                    "El nivel de la sección debe coincidir con el nivel del curso"
            );
        }

        // Buscar y validar profesor
        Usuario profesor = buscarProfesorPorDni(request.getProfesorDni());

        // Validar horarios del profesor
        if (request.getHorarios() != null && !request.getHorarios().isEmpty()) {
            validarHorarios(request.getHorarios());
            validarCruceHorariosProfesor(profesor.getId(), request.getHorarios(), -1L);
        } else {
            throw new ValidacionException("La sección debe tener al menos un horario");
        }

        // Generar código único
        String codigoGenerado = generarCodigoUnico();

        // Crear la sección
        Seccion nuevaSeccion = Seccion.builder()
                .codigo(codigoGenerado)
                .nombre(request.getNombre())
                .nivelSeccion(request.getNivelSeccion())
                .gradoSeccion(request.getGradoSeccion())
                .aula(request.getAula())
                .capacidad(request.getCapacidad())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .activa(true)
                .curso(curso)
                .profesor(profesor)
                .build();

        // Agregar horarios a la sección
        if (request.getHorarios() != null) {
            request.getHorarios().forEach(hDTO -> {
                Horario h = Horario.builder()
                        .diaSemana(hDTO.getDiaSemana())
                        .horaInicio(hDTO.getHoraInicio())
                        .horaFin(hDTO.getHoraFin())
                        .build();
                nuevaSeccion.agregarHorario(h);
            });
        }

        Seccion seccionGuardada = seccionRepository.save(nuevaSeccion);
        logger.info("Sección creada exitosamente. Sección ID: {}, Código: {}", seccionGuardada.getId(), codigoGenerado);

        return SeccionResponseDTO.deEntidad(seccionGuardada);
    }

    @Override
    @Transactional
    public SeccionResponseDTO actualizarSeccion(Long id, SeccionRequestDTO request) {
        logger.info("Actualizando sección ID: {}", id);

        Seccion seccion = seccionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada con id: " + id));

        // Validar fechas
        validarFechas(request.getFechaInicio(), request.getFechaFin());

        // Validar curso
        Curso curso = cursoRepository.findById(request.getCursoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado con id: " + request.getCursoId()));

        if (!request.getNivelSeccion().equals(curso.getNivelDestino())) {
            throw new ValidacionException(
                    "El nivel de la sección debe coincidir con el nivel del curso"
            );
        }

        // Validar profesor
        Usuario profesor = buscarProfesorPorDni(request.getProfesorDni());

        // Validar horarios del profesor (ignorando esta sección)
        if (request.getHorarios() != null && !request.getHorarios().isEmpty()) {
            validarHorarios(request.getHorarios());
            validarCruceHorariosProfesor(profesor.getId(), request.getHorarios(), id);
        } else {
            throw new ValidacionException("La sección debe tener al menos un horario");
        }

        // Validar capacidad
        int estudiantesActuales = seccion.getNumeroEstudiantesMatriculados();
        if (request.getCapacidad() < estudiantesActuales) {
            throw new ValidacionException(
                    String.format("No puedes reducir la capacidad a %d cuando ya hay %d alumnos matriculados",
                            request.getCapacidad(), estudiantesActuales)
            );
        }

        // Actualizar campos de la sección
        seccion.setNombre(request.getNombre());
        seccion.setNivelSeccion(request.getNivelSeccion());
        seccion.setGradoSeccion(request.getGradoSeccion());
        seccion.setAula(request.getAula());
        seccion.setCapacidad(request.getCapacidad());
        seccion.setFechaInicio(request.getFechaInicio());
        seccion.setFechaFin(request.getFechaFin());
        seccion.setCurso(curso);
        seccion.setProfesor(profesor);

        // Actualizar horarios (elimina los antiguos y agrega los nuevos)
        seccion.getHorarios().clear();
        if (request.getHorarios() != null) {
            request.getHorarios().forEach(hDTO -> {
                Horario h = Horario.builder()
                        .diaSemana(hDTO.getDiaSemana())
                        .horaInicio(hDTO.getHoraInicio())
                        .horaFin(hDTO.getHoraFin())
                        .build();
                seccion.agregarHorario(h);
            });
        }

        Seccion seccionActualizada = seccionRepository.save(seccion);
        logger.info("Sección actualizada exitosamente. Sección ID: {}", id);

        return SeccionResponseDTO.deEntidad(seccionActualizada);
    }

    @Override
    @Transactional
    public void eliminarSeccion(Long id) {
        logger.info("Eliminando sección ID: {}", id);

        Seccion seccion = seccionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada"));

        if (seccion.getNumeroEstudiantesMatriculados() > 0) {
            throw new ValidacionException(
                    "No se puede eliminar una sección que tiene alumnos matriculados."
            );
        }

        seccionRepository.deleteById(id);
        logger.info("Sección eliminada exitosamente");
    }

    @Override
    @Transactional
    public void desactivarSeccion(Long id) {
        logger.info("Desactivando sección ID: {}", id);

        Seccion s = seccionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada"));
        s.setActiva(false);
        seccionRepository.save(s);

        logger.info("Sección desactivada exitosamente");
    }

    @Override
    @Transactional
    public void activarSeccion(Long id) {
        logger.info("Activando sección ID: {}", id);

        Seccion s = seccionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada"));
        s.setActiva(true);
        seccionRepository.save(s);

        logger.info("Sección activada exitosamente");
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarSeccionesPorCurso(Long cursoId) {
        logger.info("Listando secciones para curso ID: {}", cursoId);

        if (!cursoRepository.existsById(cursoId)) {
            throw new RecursoNoEncontradoException("Curso no encontrado");
        }

        return seccionRepository.findByCursoId(cursoId).stream()
                .map(SeccionResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarSeccionesPorProfesor(Long profesorId) {
        logger.info("Listando secciones para profesor ID: {}", profesorId);

        return seccionRepository.findByProfesorId(profesorId).stream()
                .map(SeccionResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarSeccionesPorDniProfesor(String dni) {
        logger.info("Listando secciones para profesor con DNI: {}", dni);

        PerfilProfesor perfil = perfilProfesorRepository.findByDni(dni)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró profesor con DNI: " + dni));

        return seccionRepository.findByProfesorId(perfil.getUsuario().getId()).stream()
                .map(SeccionResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarSeccionesConCupo() {
        logger.info("Listando secciones con cupo disponible");

        return seccionRepository.findSeccionesConCupoDisponible().stream()
                .map(SeccionResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarSeccionesPorNivel(NivelAcademico nivel) {
        logger.info("Listando secciones para nivel: {}", nivel);

        return seccionRepository.findByNivelSeccionAndActivaTrue(nivel).stream()
                .map(SeccionResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    // --- MÉTODOS PRIVADOS ---

    /**
     * Valida que las fechas de inicio y fin sean válidas.
     *
     * ✅ CORREGIDO: Ahora permite fechas de inicio desde HOY
     * (Antes rechazaba fechas de hoy o en el pasado)
     */
    private void validarFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new ValidacionException("Las fechas de inicio y fin son obligatorias");
        }

        if (fechaInicio.isAfter(fechaFin)) {
            throw new ValidacionException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        // ✅ CAMBIO IMPORTANTE: Permite fecha de inicio hoy o en el futuro
        LocalDate hoy = LocalDate.now();
        if (fechaInicio.isBefore(hoy)) {
            logger.warn("Fecha de inicio anterior a hoy: {}", fechaInicio);
            throw new ValidacionException("La fecha de inicio no puede ser anterior a hoy. Hoy es: " + hoy);
        }
    }

    /**
     * Busca un profesor por su DNI.
     */
    private Usuario buscarProfesorPorDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            throw new ValidacionException("El DNI del profesor es obligatorio");
        }

        PerfilProfesor perfil = perfilProfesorRepository.findByDni(dni.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró profesor con DNI: " + dni));

        return perfil.getUsuario();
    }

    /**
     * Genera un código único para la sección.
     */
    private String generarCodigoUnico() {
        return "SEC-" + System.currentTimeMillis();
    }

    /**
     * Valida que los horarios sean estructuralmente correctos.
     */
    private void validarHorarios(List<HorarioDTO> horarios) {
        for (HorarioDTO h : horarios) {
            if (!h.isHoraValida()) {
                throw new ValidacionException(
                        String.format("El horario del %s es inválido: la hora de inicio debe ser anterior a la de fin",
                                h.getDiaSemana())
                );
            }
        }
    }

    /**
     * Valida que los nuevos horarios del profesor no choquen con sus secciones existentes.
     *
     * @param profesorId ID del profesor
     * @param horariosNuevos Lista de nuevos horarios
     * @param seccionIdIgnorar ID de sección a ignorar (para updates, -1L para creates)
     */
    private void validarCruceHorariosProfesor(Long profesorId, List<HorarioDTO> horariosNuevos, Long seccionIdIgnorar) {
        long idIgnorar = (seccionIdIgnorar == null) ? -1L : seccionIdIgnorar;

        for (HorarioDTO h : horariosNuevos) {
            // Validar estructura del horario
            if (!h.isHoraValida()) {
                throw new ValidacionException(
                        String.format("El horario del %s tiene hora de inicio posterior a la de fin",
                                h.getDiaSemana())
                );
            }

            // Consultar BD para detectar cruces
            boolean hayCruce = horarioRepository.existeCruceProfesor(
                    profesorId,
                    h.getDiaSemana(),
                    h.getHoraInicio(),
                    h.getHoraFin(),
                    idIgnorar
            );

            if (hayCruce) {
                throw new ValidacionException(
                        String.format("El profesor ya tiene una clase asignada el %s entre %s y %s en otra sección",
                                h.getDiaSemana(), h.getHoraInicio(), h.getHoraFin())
                );
            }
        }
    }
}