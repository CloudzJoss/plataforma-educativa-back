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
import com.proyecto.fundaciondeportiva.model.enums.Rol;
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
    private HorarioRepository horarioRepository; // ✅ Necesario para validar cruces

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarTodasLasSecciones() {
        return seccionRepository.findAll().stream().map(SeccionResponseDTO::deEntidad).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarSeccionesActivas() {
        return seccionRepository.findByActivaTrue().stream().map(SeccionResponseDTO::deEntidad).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SeccionResponseDTO obtenerSeccionPorId(Long id) {
        Seccion seccion = seccionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada con id: " + id));
        return SeccionResponseDTO.deEntidad(seccion);
    }

    @Override
    @Transactional
    public SeccionResponseDTO crearSeccion(SeccionRequestDTO request) {
        logger.info("Creando nueva sección: {}", request.getNombre());
        validarFechas(request.getFechaInicio(), request.getFechaFin());

        Curso curso = cursoRepository.findById(request.getCursoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado con id: " + request.getCursoId()));

        if (!request.getNivelSeccion().equals(curso.getNivelDestino())) {
            throw new ValidacionException("El nivel de la sección debe coincidir con el nivel del curso");
        }

        Usuario profesor = buscarProfesorPorDni(request.getProfesorDni());

        // ✅ VALIDAR CRUCE DE HORARIOS (PROFESOR)
        // Pasamos -1L como ID a ignorar porque es una creación nueva
        if (request.getHorarios() != null && !request.getHorarios().isEmpty()) {
            validarCruceHorariosProfesor(profesor.getId(), request.getHorarios(), -1L);
        }

        String codigoGenerado = generarCodigoUnico();

        Seccion nuevaSeccion = Seccion.builder()
                .codigo(codigoGenerado)
                .nombre(request.getNombre())
                .nivelSeccion(request.getNivelSeccion())
                .gradoSeccion(request.getGradoSeccion())
                // .turno(...) // ❌ Eliminado
                .aula(request.getAula())
                .capacidad(request.getCapacidad())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .activa(true)
                .curso(curso)
                .profesor(profesor)
                .build();

        // ✅ GUARDAR HORARIOS
        if (request.getHorarios() != null) {
            request.getHorarios().forEach(hDTO -> {
                Horario h = Horario.builder()
                        .diaSemana(hDTO.getDiaSemana())
                        .horaInicio(hDTO.getHoraInicio())
                        .horaFin(hDTO.getHoraFin())
                        .build();
                nuevaSeccion.agregarHorario(h); // Método helper en Entidad Seccion
            });
        }

        return SeccionResponseDTO.deEntidad(seccionRepository.save(nuevaSeccion));
    }

    @Override
    @Transactional
    public SeccionResponseDTO actualizarSeccion(Long id, SeccionRequestDTO request) {
        Seccion seccion = seccionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada con id: " + id));

        validarFechas(request.getFechaInicio(), request.getFechaFin());

        Curso curso = cursoRepository.findById(request.getCursoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado con id: " + request.getCursoId()));

        if (!request.getNivelSeccion().equals(curso.getNivelDestino())) {
            throw new ValidacionException("El nivel de la sección debe coincidir con el nivel del curso");
        }

        Usuario profesor = buscarProfesorPorDni(request.getProfesorDni());

        // ✅ VALIDAR CRUCE DE HORARIOS (PROFESOR)
        // Pasamos el ID actual 'id' para que la query ignore los horarios de ESTA misma sección al validar
        if (request.getHorarios() != null && !request.getHorarios().isEmpty()) {
            validarCruceHorariosProfesor(profesor.getId(), request.getHorarios(), id);
        }

        seccion.setNombre(request.getNombre());
        seccion.setNivelSeccion(request.getNivelSeccion());
        seccion.setGradoSeccion(request.getGradoSeccion());
        // seccion.setTurno(...) // ❌ Eliminado
        seccion.setAula(request.getAula());
        seccion.setCapacidad(request.getCapacidad());
        seccion.setFechaInicio(request.getFechaInicio());
        seccion.setFechaFin(request.getFechaFin());
        seccion.setCurso(curso);
        seccion.setProfesor(profesor);

        // ✅ ACTUALIZAR HORARIOS
        seccion.getHorarios().clear(); // Limpia los anteriores (orphanRemoval los borrará de BD)
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

        return SeccionResponseDTO.deEntidad(seccionRepository.save(seccion));
    }

    @Override
    @Transactional
    public void eliminarSeccion(Long id) {
        Seccion seccion = seccionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada"));

        if (seccion.getNumeroEstudiantesMatriculados() > 0) {
            throw new ValidacionException("No se puede eliminar una sección que tiene alumnos matriculados.");
        }
        seccionRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void desactivarSeccion(Long id) {
        Seccion s = seccionRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada"));
        s.setActiva(false);
        seccionRepository.save(s);
    }

    @Override
    @Transactional
    public void activarSeccion(Long id) {
        Seccion s = seccionRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada"));
        s.setActiva(true);
        seccionRepository.save(s);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarSeccionesPorCurso(Long cursoId) {
        if (!cursoRepository.existsById(cursoId)) throw new RecursoNoEncontradoException("Curso no encontrado");
        return seccionRepository.findByCursoId(cursoId).stream().map(SeccionResponseDTO::deEntidad).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarSeccionesPorProfesor(Long profesorId) {
        return seccionRepository.findByProfesorId(profesorId).stream().map(SeccionResponseDTO::deEntidad).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarSeccionesPorDniProfesor(String dni) {
        PerfilProfesor perfil = perfilProfesorRepository.findByDni(dni)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró profesor con DNI: " + dni));
        return seccionRepository.findByProfesorId(perfil.getUsuario().getId()).stream().map(SeccionResponseDTO::deEntidad).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarSeccionesConCupo() {
        return seccionRepository.findSeccionesConCupoDisponible().stream().map(SeccionResponseDTO::deEntidad).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarSeccionesPorNivel(NivelAcademico nivel) {
        return seccionRepository.findByNivelSeccionAndActivaTrue(nivel).stream().map(SeccionResponseDTO::deEntidad).collect(Collectors.toList());
    }

    // --- MÉTODOS PRIVADOS ---

    private void validarFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) throw new ValidacionException("Las fechas son obligatorias");
        if (fechaInicio.isAfter(fechaFin)) throw new ValidacionException("Fecha inicio posterior a fin");
    }

    private Usuario buscarProfesorPorDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) throw new ValidacionException("DNI profesor obligatorio");
        PerfilProfesor perfil = perfilProfesorRepository.findByDni(dni.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró profesor con DNI: " + dni));
        return perfil.getUsuario();
    }

    private String generarCodigoUnico() {
        return "SEC-" + System.currentTimeMillis();
    }

    /**
     * Valida que los nuevos horarios no choquen con los existentes del profesor.
     */
    private void validarCruceHorariosProfesor(Long profesorId, List<HorarioDTO> horariosNuevos, Long seccionIdIgnorar) {
        long idIgnorar = (seccionIdIgnorar == null) ? -1L : seccionIdIgnorar;

        for (HorarioDTO h : horariosNuevos) {
            // Lógica básica: Inicio debe ser antes que Fin
            if (!h.getHoraInicio().isBefore(h.getHoraFin())) {
                throw new ValidacionException("En el horario del " + h.getDiaSemana() + ", la hora de inicio debe ser anterior a la de fin.");
            }

            // Consulta a la BD usando el repositorio
            boolean hayCruce = horarioRepository.existeCruceProfesor(
                    profesorId,
                    h.getDiaSemana(),
                    h.getHoraInicio(),
                    h.getHoraFin(),
                    idIgnorar
            );

            if (hayCruce) {
                throw new ValidacionException(
                        String.format("El profesor ya tiene una clase asignada el %s entre %s y %s en otra sección.",
                                h.getDiaSemana(), h.getHoraInicio(), h.getHoraFin())
                );
            }
        }
    }
}