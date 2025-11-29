package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.request.SeccionRequestDTO;
import com.proyecto.fundaciondeportiva.dto.response.SeccionResponseDTO;
import com.proyecto.fundaciondeportiva.dto.response.UsuarioResponse;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import com.proyecto.fundaciondeportiva.repository.SeccionRepository;
import com.proyecto.fundaciondeportiva.service.ServicioSeccion;
import com.proyecto.fundaciondeportiva.service.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de Secciones.
 * Maneja operaciones de creación, actualización y listado de secciones.
 */
@RestController
@RequestMapping("/api/secciones")
public class SeccionController {

    private static final Logger logger = LoggerFactory.getLogger(SeccionController.class);

    @Autowired
    private ServicioSeccion servicioSeccion;

    @Autowired
    private UsuarioService servicioUsuario;

    @Autowired
    private SeccionRepository seccionRepository;

    // ==================== ENDPOINTS DE PROFESOR ====================

    /**
     * El profesor obtiene sus propias secciones asignadas.
     * GET /api/secciones/mis-secciones
     */
    @GetMapping("/mis-secciones")
    @PreAuthorize("hasRole('PROFESOR')")
    public ResponseEntity<List<SeccionResponseDTO>> obtenerMisSecciones() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String emailProfesor = auth.getName();

            logger.info("Profesor {} consulta sus secciones", emailProfesor);

            // Obtener usuario completo
            UsuarioResponse usuarioDTO = servicioUsuario.obtenerUsuarioResponsePorEmail(emailProfesor);

            // Validar que tiene perfil de profesor
            if (usuarioDTO.getDniProfesor() == null) {
                throw new RuntimeException("El usuario no tiene perfil de profesor asociado");
            }

            String dniProfesor = usuarioDTO.getDniProfesor();
            List<SeccionResponseDTO> secciones = servicioSeccion.listarSeccionesPorDniProfesor(dniProfesor);

            return ResponseEntity.ok(secciones);

        } catch (Exception e) {
            logger.error("Error en endpoint obtenerMisSecciones", e);
            throw e;
        }
    }

    // ==================== ENDPOINTS DE ADMINISTRADOR ====================

    /**
     * Crear una nueva sección.
     * POST /api/secciones
     * Body: SeccionRequestDTO con nombre, nivel, grado, horarios, etc.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<SeccionResponseDTO> crearSeccion(
            @Valid @RequestBody SeccionRequestDTO request) {
        try {
            logger.info("Admin solicita crear nueva sección: {}", request.getNombre());

            SeccionResponseDTO seccion = servicioSeccion.crearSeccion(request);

            return new ResponseEntity<>(seccion, HttpStatus.CREATED);

        } catch (Exception e) {
            logger.error("Error en endpoint crearSeccion", e);
            throw e;
        }
    }

    /**
     * Actualizar una sección existente.
     * PUT /api/secciones/{id}
     * Body: SeccionRequestDTO con los nuevos datos
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<SeccionResponseDTO> actualizarSeccion(
            @PathVariable Long id,
            @Valid @RequestBody SeccionRequestDTO request) {
        try {
            logger.info("Admin solicita actualizar sección ID: {}", id);

            SeccionResponseDTO seccion = servicioSeccion.actualizarSeccion(id, request);

            return ResponseEntity.ok(seccion);

        } catch (Exception e) {
            logger.error("Error en endpoint actualizarSeccion", e);
            throw e;
        }
    }

    /**
     * Eliminar una sección.
     * Solo es posible si no tiene alumnos matriculados.
     * DELETE /api/secciones/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarSeccion(@PathVariable Long id) {
        try {
            logger.info("Admin solicita eliminar sección ID: {}", id);

            servicioSeccion.eliminarSeccion(id);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            logger.error("Error en endpoint eliminarSeccion", e);
            throw e;
        }
    }

    /**
     * Desactivar una sección (no elimina, solo marca como inactiva).
     * PATCH /api/secciones/{id}/desactivar
     */
    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> desactivarSeccion(@PathVariable Long id) {
        try {
            logger.info("Admin solicita desactivar sección ID: {}", id);

            servicioSeccion.desactivarSeccion(id);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            logger.error("Error en endpoint desactivarSeccion", e);
            throw e;
        }
    }

    /**
     * Activar una sección previamente desactivada.
     * PATCH /api/secciones/{id}/activar
     */
    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> activarSeccion(@PathVariable Long id) {
        try {
            logger.info("Admin solicita activar sección ID: {}", id);

            servicioSeccion.activarSeccion(id);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            logger.error("Error en endpoint activarSeccion", e);
            throw e;
        }
    }

    // ==================== ENDPOINTS DE CONSULTA (AUTENTICADOS) ====================

    /**
     * Listar todas las secciones del sistema.
     * GET /api/secciones
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeccionResponseDTO>> listarTodasLasSecciones() {
        try {
            logger.info("Listando todas las secciones");

            List<SeccionResponseDTO> secciones = servicioSeccion.listarTodasLasSecciones();

            return ResponseEntity.ok(secciones);

        } catch (Exception e) {
            logger.error("Error en endpoint listarTodasLasSecciones", e);
            throw e;
        }
    }

    /**
     * Listar solo secciones activas.
     * GET /api/secciones/activas
     */
    @GetMapping("/activas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeccionResponseDTO>> listarSeccionesActivas() {
        try {
            logger.info("Listando secciones activas");

            List<SeccionResponseDTO> secciones = servicioSeccion.listarSeccionesActivas();

            return ResponseEntity.ok(secciones);

        } catch (Exception e) {
            logger.error("Error en endpoint listarSeccionesActivas", e);
            throw e;
        }
    }

    /**
     * Obtener una sección específica por ID.
     * GET /api/secciones/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SeccionResponseDTO> obtenerSeccionPorId(@PathVariable Long id) {
        try {
            logger.info("Obteniendo sección ID: {}", id);

            SeccionResponseDTO seccion = servicioSeccion.obtenerSeccionPorId(id);

            return ResponseEntity.ok(seccion);

        } catch (Exception e) {
            logger.error("Error en endpoint obtenerSeccionPorId", e);
            throw e;
        }
    }

    /**
     * Listar secciones de un curso específico.
     * GET /api/secciones/curso/{cursoId}
     */
    @GetMapping("/curso/{cursoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeccionResponseDTO>> listarSeccionesPorCurso(
            @PathVariable Long cursoId) {
        try {
            logger.info("Listando secciones para curso ID: {}", cursoId);

            List<SeccionResponseDTO> secciones = servicioSeccion.listarSeccionesPorCurso(cursoId);

            return ResponseEntity.ok(secciones);

        } catch (Exception e) {
            logger.error("Error en endpoint listarSeccionesPorCurso", e);
            throw e;
        }
    }

    /**
     * Listar secciones de un profesor específico.
     * GET /api/secciones/profesor/{profesorId}
     */
    @GetMapping("/profesor/{profesorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeccionResponseDTO>> listarSeccionesPorProfesor(
            @PathVariable Long profesorId) {
        try {
            logger.info("Listando secciones para profesor ID: {}", profesorId);

            List<SeccionResponseDTO> secciones = servicioSeccion.listarSeccionesPorProfesor(profesorId);

            return ResponseEntity.ok(secciones);

        } catch (Exception e) {
            logger.error("Error en endpoint listarSeccionesPorProfesor", e);
            throw e;
        }
    }

    /**
     * Listar secciones de un nivel académico específico.
     * GET /api/secciones/nivel/{nivel}
     * Niveles: INICIAL, PRIMARIA, SECUNDARIA
     */
    @GetMapping("/nivel/{nivel}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeccionResponseDTO>> listarSeccionesPorNivel(
            @PathVariable NivelAcademico nivel) {
        try {
            logger.info("Listando secciones para nivel: {}", nivel);

            List<SeccionResponseDTO> secciones = servicioSeccion.listarSeccionesPorNivel(nivel);

            return ResponseEntity.ok(secciones);

        } catch (Exception e) {
            logger.error("Error en endpoint listarSeccionesPorNivel", e);
            throw e;
        }
    }

    /**
     * Listar secciones que aún tienen cupo disponible.
     * GET /api/secciones/con-cupo
     */
    @GetMapping("/con-cupo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeccionResponseDTO>> listarSeccionesConCupo() {
        try {
            logger.info("Listando secciones con cupo disponible");

            List<SeccionResponseDTO> secciones = servicioSeccion.listarSeccionesConCupo();

            return ResponseEntity.ok(secciones);

        } catch (Exception e) {
            logger.error("Error en endpoint listarSeccionesConCupo", e);
            throw e;
        }
    }
}