package com.proyecto.fundaciondeportiva.service.impl;

import com.proyecto.fundaciondeportiva.dto.request.HorarioDTO;
import com.proyecto.fundaciondeportiva.dto.request.SeccionRequestDTO;
import com.proyecto.fundaciondeportiva.dto.response.SeccionResponseDTO;
import com.proyecto.fundaciondeportiva.exception.RecursoNoEncontradoException;
import com.proyecto.fundaciondeportiva.exception.ValidacionException;
import com.proyecto.fundaciondeportiva.model.entity.*;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import com.proyecto.fundaciondeportiva.repository.*;
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

    // ✅ INYECCIÓN NECESARIA PARA LAS SESIONES
    @Autowired
    private SesionRepository sesionRepository;

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

        validarFechas(request.getFechaInicio(), request.getFechaFin());

        Curso curso = cursoRepository.findById(request.getCursoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado con id: " + request.getCursoId()));

        if (!request.getNivelSeccion().equals(curso.getNivelDestino())) {
            throw new ValidacionException("El nivel de la sección debe coincidir con el nivel del curso");
        }

        Usuario profesor = buscarProfesorPorDni(request.getProfesorDni());

        if (request.getHorarios() != null && !request.getHorarios().isEmpty()) {
            validarHorarios(request.getHorarios());
            validarCruceHorariosProfesor(profesor.getId(), request.getHorarios(), -1L);
        } else {
            throw new ValidacionException("La sección debe tener al menos un horario");
        }

        String codigoGenerado = generarCodigoUnico();

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

        // ✅ GENERACIÓN AUTOMÁTICA DE SESIONES
        generarSesionesAutomaticas(seccionGuardada);

        logger.info("Sección creada exitosamente. Sección ID: {}, Código: {}", seccionGuardada.getId(), codigoGenerado);
        return SeccionResponseDTO.deEntidad(seccionGuardada);
    }

    @Override
    @Transactional
    public SeccionResponseDTO actualizarSeccion(Long id, SeccionRequestDTO request) {
        logger.info("Actualizando sección ID: {}", id);

        Seccion seccion = seccionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada con id: " + id));

        validarFechas(request.getFechaInicio(), request.getFechaFin());

        Curso curso = cursoRepository.findById(request.getCursoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado con id: " + request.getCursoId()));

        if (!request.getNivelSeccion().equals(curso.getNivelDestino())) {
            throw new ValidacionException("El nivel de la sección debe coincidir con el nivel del curso");
        }

        Usuario profesor = buscarProfesorPorDni(request.getProfesorDni());

        if (request.getHorarios() != null && !request.getHorarios().isEmpty()) {
            validarHorarios(request.getHorarios());
            validarCruceHorariosProfesor(profesor.getId(), request.getHorarios(), id);
        } else {
            throw new ValidacionException("La sección debe tener al menos un horario");
        }

        int estudiantesActuales = seccion.getNumeroEstudiantesMatriculados();
        if (request.getCapacidad() < estudiantesActuales) {
            throw new ValidacionException(String.format("No puedes reducir la capacidad a %d cuando ya hay %d alumnos matriculados", request.getCapacidad(), estudiantesActuales));
        }

        // Detectar si cambiaron fechas u horarios para saber si regenerar sesiones
        boolean fechasCambiaron = !seccion.getFechaInicio().isEqual(request.getFechaInicio()) ||
                !seccion.getFechaFin().isEqual(request.getFechaFin());
        // Simplificación: asumimos que si se llama a update, los horarios podrían haber cambiado.
        // Lo ideal sería comparar la lista, pero regenerar es más seguro.

        seccion.setNombre(request.getNombre());
        seccion.setNivelSeccion(request.getNivelSeccion());
        seccion.setGradoSeccion(request.getGradoSeccion());
        seccion.setAula(request.getAula());
        seccion.setCapacidad(request.getCapacidad());
        seccion.setFechaInicio(request.getFechaInicio());
        seccion.setFechaFin(request.getFechaFin());
        seccion.setCurso(curso);
        seccion.setProfesor(profesor);

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

        // ✅ REGENERACIÓN DE SESIONES (Si se edita la sección, recalculamos el calendario)
        // 1. Borrar sesiones futuras/todas (depende de tu regla de negocio, aquí borramos todas para regenerar limpio)
        sesionRepository.deleteBySeccionId(id);

        // 2. Crear nuevas
        generarSesionesAutomaticas(seccionActualizada);

        logger.info("Sección actualizada y calendario regenerado. Sección ID: {}", id);
        return SeccionResponseDTO.deEntidad(seccionActualizada);
    }

    @Override
    @Transactional
    public void eliminarSeccion(Long id) {
        logger.info("Eliminando sección ID: {}", id);
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

    /**
     * ✅ LÓGICA PRINCIPAL: Genera las sesiones en base a fechas y horarios
     */
    private void generarSesionesAutomaticas(Seccion seccion) {
        LocalDate inicio = seccion.getFechaInicio();
        LocalDate fin = seccion.getFechaFin();
        List<Horario> horariosConfigurados = seccion.getHorarios();

        if (horariosConfigurados == null || horariosConfigurados.isEmpty()) {
            return;
        }

        // Recorrer día por día el rango de fechas (Java 9+ datesUntil)
        List<Sesion> sesionesAGenerar = inicio.datesUntil(fin.plusDays(1))
                .filter(fecha -> {
                    // Verificar si la fecha cae en un día de clase (Lunes, Martes...)
                    return horariosConfigurados.stream()
                            .anyMatch(h -> h.getDiaSemana() == fecha.getDayOfWeek());
                })
                .map(fecha -> {
                    // Crear una sesión por cada bloque horario de ese día
                    return horariosConfigurados.stream()
                            .filter(h -> h.getDiaSemana() == fecha.getDayOfWeek())
                            .map(horario -> Sesion.builder()
                                    .fecha(fecha)
                                    .horaInicio(horario.getHoraInicio())
                                    .horaFin(horario.getHoraFin())
                                    .tema(null) // Se deja vacío para que el profe lo llene luego
                                    .seccion(seccion)
                                    .build())
                            .collect(Collectors.toList());
                })
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (!sesionesAGenerar.isEmpty()) {
            sesionRepository.saveAll(sesionesAGenerar);
            logger.info("✅ Se generaron {} sesiones automáticas para la sección {}", sesionesAGenerar.size(), seccion.getCodigo());
        }
    }

    private void validarFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) throw new ValidacionException("Las fechas son obligatorias");
        if (fechaInicio.isAfter(fechaFin)) throw new ValidacionException("Inicio no puede ser después del fin");

        LocalDate hoy = LocalDate.now();
        if (fechaInicio.isBefore(hoy)) {
            logger.warn("Fecha inicio anterior a hoy: {}", fechaInicio);
            throw new ValidacionException("La fecha de inicio no puede ser anterior a hoy");
        }
    }

    private Usuario buscarProfesorPorDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) throw new ValidacionException("DNI obligatorio");
        return perfilProfesorRepository.findByDni(dni.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesor no encontrado con DNI: " + dni)).getUsuario();
    }

    private String generarCodigoUnico() {
        return "SEC-" + System.currentTimeMillis();
    }

    private void validarHorarios(List<HorarioDTO> horarios) {
        for (HorarioDTO h : horarios) {
            if (!h.isHoraValida()) throw new ValidacionException("Horario inválido: inicio > fin en " + h.getDiaSemana());
        }
    }

    private void validarCruceHorariosProfesor(Long profesorId, List<HorarioDTO> horariosNuevos, Long seccionIdIgnorar) {
        long idIgnorar = (seccionIdIgnorar == null) ? -1L : seccionIdIgnorar;
        for (HorarioDTO h : horariosNuevos) {
            if (!h.isHoraValida()) throw new ValidacionException("Horario inválido");
            boolean hayCruce = horarioRepository.existeCruceProfesor(profesorId, h.getDiaSemana(), h.getHoraInicio(), h.getHoraFin(), idIgnorar);
            if (hayCruce) throw new ValidacionException("El profesor ya tiene clase el " + h.getDiaSemana() + " en ese horario");
        }
    }
}