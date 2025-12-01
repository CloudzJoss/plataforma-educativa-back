package com.proyecto.fundaciondeportiva.service.impl;

import com.proyecto.fundaciondeportiva.dto.request.CursoRequestDTO;
import com.proyecto.fundaciondeportiva.dto.response.CursoResponseDTO;
import com.proyecto.fundaciondeportiva.exception.RecursoNoEncontradoException;
import com.proyecto.fundaciondeportiva.exception.ValidacionException;
import com.proyecto.fundaciondeportiva.model.entity.Curso;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import com.proyecto.fundaciondeportiva.repository.CursoRepository;
import com.proyecto.fundaciondeportiva.repository.UsuarioRepository;
import com.proyecto.fundaciondeportiva.service.ServicioCurso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de la lógica de negocio para la gestión de Cursos.
 * Ahora con generación automática de códigos de curso.
 */
@Service
public class ServicioCursoImpl implements ServicioCurso {

    private static final Logger logger = LoggerFactory.getLogger(ServicioCursoImpl.class);

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CursoResponseDTO> listarTodosLosCursos() {
        logger.info("Listando todos los cursos");
        return cursoRepository.findAll()
                .stream()
                .map(CursoResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CursoResponseDTO obtenerCursoPorId(Long id) {
        logger.info("Obteniendo curso con id: {}", id);
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado con id: " + id));
        return CursoResponseDTO.deEntidad(curso);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CursoResponseDTO> listarCursosPorNivel(NivelAcademico nivel) {
        logger.info("Listando cursos por nivel: {}", nivel);
        return cursoRepository.findByNivelDestino(nivel)
                .stream()
                .map(CursoResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CursoResponseDTO crearCurso(CursoRequestDTO request, String emailAdmin) {
        logger.info("Iniciando creación de curso. Email admin: {}", emailAdmin);
        logger.debug("Datos del curso: titulo={}, nivel={}", request.getTitulo(), request.getNivelDestino());

        try {
            // Buscar usuario administrador
            logger.debug("Buscando usuario administrador con email: {}", emailAdmin);
            Usuario admin = usuarioRepository.findByEmail(emailAdmin)
                    .orElseThrow(() -> {
                        logger.error("Usuario administrador no encontrado: {}", emailAdmin);
                        return new RecursoNoEncontradoException("Usuario administrador no encontrado: " + emailAdmin);
                    });

            // ✅ CORREGIDO: Uso de getNombres() y getApellidos() en el log
            logger.info("Usuario administrador encontrado: {} {} (ID: {})",
                    admin.getNombres(), admin.getApellidos(), admin.getId());

            // Generar código único automáticamente
            String codigoGenerado = generarCodigoUnico(request.getNivelDestino());
            logger.info("Código generado automáticamente: {}", codigoGenerado);

            // Crear nuevo curso
            Curso nuevoCurso = Curso.builder()
                    .codigo(codigoGenerado)
                    .titulo(request.getTitulo())
                    .descripcion(request.getDescripcion())
                    .nivelDestino(request.getNivelDestino())
                    .creadoPor(admin)
                    .build();

            logger.debug("Guardando curso en base de datos...");
            Curso cursoGuardado = cursoRepository.save(nuevoCurso);
            logger.info("Curso creado exitosamente con ID: {} y código: {}", cursoGuardado.getId(), cursoGuardado.getCodigo());

            return CursoResponseDTO.deEntidad(cursoGuardado);

        } catch (RecursoNoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al crear curso", e);
            throw new RuntimeException("Error al crear el curso: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public CursoResponseDTO actualizarCurso(Long id, CursoRequestDTO request) {
        logger.info("Actualizando curso con id: {}", id);

        try {
            Curso cursoExistente = cursoRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Curso no encontrado con id: {}", id);
                        return new RecursoNoEncontradoException("Curso no encontrado con id: " + id);
                    });

            // Actualizar solo título, descripción y nivel
            // El código NO se puede cambiar
            cursoExistente.setTitulo(request.getTitulo());
            cursoExistente.setDescripcion(request.getDescripcion());
            cursoExistente.setNivelDestino(request.getNivelDestino());

            Curso cursoActualizado = cursoRepository.save(cursoExistente);
            logger.info("Curso actualizado exitosamente: {}", cursoActualizado.getId());

            return CursoResponseDTO.deEntidad(cursoActualizado);

        } catch (RecursoNoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar curso", e);
            throw new RuntimeException("Error al actualizar el curso: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void eliminarCurso(Long id) {
        logger.info("Eliminando curso con id: {}", id);

        if (!cursoRepository.existsById(id)) {
            logger.error("Intento de eliminar curso inexistente: {}", id);
            throw new RecursoNoEncontradoException("Curso no encontrado con id: " + id);
        }

        try {
            cursoRepository.deleteById(id);
            logger.info("Curso eliminado exitosamente: {}", id);
        } catch (Exception e) {
            logger.error("Error al eliminar curso", e);
            throw new RuntimeException("Error al eliminar el curso: " + e.getMessage(), e);
        }
    }

    /**
     * Genera un código único para el curso basado en el nivel académico.
     * Formato: [PREFIJO]-[NÚMERO]
     * Ejemplos: INI-001, PRI-001, SEC-001
     */
    private String generarCodigoUnico(NivelAcademico nivel) {
        String prefijo = obtenerPrefijoPorNivel(nivel);

        // Buscar todos los cursos con este prefijo
        List<Curso> cursosDelNivel = cursoRepository.findByNivelDestino(nivel);

        // Encontrar el número más alto
        int maxNumero = 0;
        for (Curso curso : cursosDelNivel) {
            String codigo = curso.getCodigo();
            if (codigo.startsWith(prefijo + "-")) {
                try {
                    String numeroStr = codigo.substring(prefijo.length() + 1);
                    int numero = Integer.parseInt(numeroStr);
                    if (numero > maxNumero) {
                        maxNumero = numero;
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Código de curso no numérico encontrado: {}", codigo);
                }
            }
        }

        // Generar el siguiente número
        int siguienteNumero = maxNumero + 1;
        String codigoGenerado = String.format("%s-%03d", prefijo, siguienteNumero);

        // Verificar que no exista (por seguridad)
        int intentos = 0;
        while (cursoRepository.existsByCodigo(codigoGenerado) && intentos < 100) {
            siguienteNumero++;
            codigoGenerado = String.format("%s-%03d", prefijo, siguienteNumero);
            intentos++;
        }

        if (intentos >= 100) {
            throw new RuntimeException("No se pudo generar un código único después de 100 intentos");
        }

        return codigoGenerado;
    }

    /**
     * Obtiene el prefijo del código según el nivel académico.
     */
    private String obtenerPrefijoPorNivel(NivelAcademico nivel) {
        switch (nivel) {
            case INICIAL:
                return "INI";
            case PRIMARIA:
                return "PRI";
            case SECUNDARIA:
                return "SEC";
            default:
                throw new ValidacionException("Nivel académico no válido: " + nivel);
        }
    }
}