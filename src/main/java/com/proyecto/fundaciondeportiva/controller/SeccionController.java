package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.request.SeccionRequestDTO;
import com.proyecto.fundaciondeportiva.dto.response.SeccionResponseDTO;
import com.proyecto.fundaciondeportiva.dto.response.UsuarioResponse;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import com.proyecto.fundaciondeportiva.model.enums.Turno;
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

@RestController
@RequestMapping("/api/secciones")
public class SeccionController {

    private static final Logger logger = LoggerFactory.getLogger(SeccionController.class);

    @Autowired
    private ServicioSeccion servicioSeccion;

    @Autowired
    private UsuarioService servicioUsuario;

    /**
     * Obtener las secciones del profesor autenticado
     * GET /api/secciones/mis-secciones
     */
    @GetMapping("/mis-secciones")
    @PreAuthorize("hasRole('PROFESOR')")
    public ResponseEntity<List<SeccionResponseDTO>> obtenerMisSecciones() {
        try {
            // 1. Obtener el email del usuario autenticado desde el JWT
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String emailProfesor = auth.getName();

            logger.info("Solicitud de secciones del profesor con email: {}", emailProfesor);

            // 2. Obtener el perfil completo del profesor (usando el método correcto)
            UsuarioResponse usuarioDTO = servicioUsuario.obtenerUsuarioResponsePorEmail(emailProfesor); // 👈 CORREGIDO

            // 3. Validar que tenga perfil de profesor
            if (usuarioDTO.getDniProfesor() == null) { // 👈 CORREGIDO
                throw new RuntimeException("El usuario no tiene un perfil de profesor asociado");
            }

            String dniProfesor = usuarioDTO.getDniProfesor(); // 👈 CORREGIDO
            logger.info("DNI del profesor identificado: {}", dniProfesor);

            // 4. Buscar secciones por DNI del profesor
            List<SeccionResponseDTO> secciones = servicioSeccion.listarSeccionesPorDniProfesor(dniProfesor);
            logger.info("Se encontraron {} secciones para el profesor", secciones.size());

            return ResponseEntity.ok(secciones);

        } catch (Exception e) {
            logger.error("Error en endpoint obtenerMisSecciones", e);
            throw e;
        }
    }

    // --- RESTO DE ENDPOINTS (sin cambios) ---

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<SeccionResponseDTO> crearSeccion(@Valid @RequestBody SeccionRequestDTO request) {
        try {
            logger.info("Solicitud de creación de sección recibida");
            SeccionResponseDTO seccionCreada = servicioSeccion.crearSeccion(request);
            return new ResponseEntity<>(seccionCreada, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error en endpoint crearSeccion", e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<SeccionResponseDTO> actualizarSeccion(
            @PathVariable Long id,
            @Valid @RequestBody SeccionRequestDTO request) {
        try {
            logger.info("Solicitud de actualización de sección ID: {}", id);
            SeccionResponseDTO seccionActualizada = servicioSeccion.actualizarSeccion(id, request);
            return ResponseEntity.ok(seccionActualizada);
        } catch (Exception e) {
            logger.error("Error en endpoint actualizarSeccion", e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarSeccion(@PathVariable Long id) {
        try {
            logger.info("Solicitud de eliminación de sección ID: {}", id);
            servicioSeccion.eliminarSeccion(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error en endpoint eliminarSeccion", e);
            throw e;
        }
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> desactivarSeccion(@PathVariable Long id) {
        try {
            logger.info("Solicitud de desactivación de sección ID: {}", id);
            servicioSeccion.desactivarSeccion(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error en endpoint desactivarSeccion", e);
            throw e;
        }
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> activarSeccion(@PathVariable Long id) {
        try {
            logger.info("Solicitud de activación de sección ID: {}", id);
            servicioSeccion.activarSeccion(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error en endpoint activarSeccion", e);
            throw e;
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeccionResponseDTO>> listarTodasLasSecciones() {
        try {
            logger.info("Solicitud de listado de todas las secciones");
            List<SeccionResponseDTO> secciones = servicioSeccion.listarTodasLasSecciones();
            return ResponseEntity.ok(secciones);
        } catch (Exception e) {
            logger.error("Error en endpoint listarTodasLasSecciones", e);
            throw e;
        }
    }

    @GetMapping("/activas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeccionResponseDTO>> listarSeccionesActivas() {
        try {
            logger.info("Solicitud de listado de secciones activas");
            List<SeccionResponseDTO> secciones = servicioSeccion.listarSeccionesActivas();
            return ResponseEntity.ok(secciones);
        } catch (Exception e) {
            logger.error("Error en endpoint listarSeccionesActivas", e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SeccionResponseDTO> obtenerSeccionPorId(@PathVariable Long id) {
        try {
            logger.info("Solicitud de sección por ID: {}", id);
            SeccionResponseDTO seccion = servicioSeccion.obtenerSeccionPorId(id);
            return ResponseEntity.ok(seccion);
        } catch (Exception e) {
            logger.error("Error en endpoint obtenerSeccionPorId", e);
            throw e;
        }
    }

    @GetMapping("/curso/{cursoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeccionResponseDTO>> listarSeccionesPorCurso(@PathVariable Long cursoId) {
        try {
            logger.info("Solicitud de secciones del curso ID: {}", cursoId);
            List<SeccionResponseDTO> secciones = servicioSeccion.listarSeccionesPorCurso(cursoId);
            return ResponseEntity.ok(secciones);
        } catch (Exception e) {
            logger.error("Error en endpoint listarSeccionesPorCurso", e);
            throw e;
        }
    }

    @GetMapping("/profesor/{profesorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeccionResponseDTO>> listarSeccionesPorProfesor(@PathVariable Long profesorId) {
        try {
            logger.info("Solicitud de secciones del profesor ID: {}", profesorId);
            List<SeccionResponseDTO> secciones = servicioSeccion.listarSeccionesPorProfesor(profesorId);
            return ResponseEntity.ok(secciones);
        } catch (Exception e) {
            logger.error("Error en endpoint listarSeccionesPorProfesor", e);
            throw e;
        }
    }

    @GetMapping("/turno/{turno}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeccionResponseDTO>> listarSeccionesPorTurno(@PathVariable Turno turno) {
        try {
            logger.info("Solicitud de secciones del turno: {}", turno);
            List<SeccionResponseDTO> secciones = servicioSeccion.listarSeccionesPorTurno(turno);
            return ResponseEntity.ok(secciones);
        } catch (Exception e) {
            logger.error("Error en endpoint listarSeccionesPorTurno", e);
            throw e;
        }
    }

    @GetMapping("/nivel/{nivel}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeccionResponseDTO>> listarSeccionesPorNivel(@PathVariable NivelAcademico nivel) {
        try {
            logger.info("Solicitud de secciones del nivel: {}", nivel);
            List<SeccionResponseDTO> secciones = servicioSeccion.listarSeccionesPorNivel(nivel);
            return ResponseEntity.ok(secciones);
        } catch (Exception e) {
            logger.error("Error en endpoint listarSeccionesPorNivel", e);
            throw e;
        }
    }

    @GetMapping("/con-cupo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeccionResponseDTO>> listarSeccionesConCupo() {
        try {
            logger.info("Solicitud de secciones con cupo disponible");
            List<SeccionResponseDTO> secciones = servicioSeccion.listarSeccionesConCupo();
            return ResponseEntity.ok(secciones);
        } catch (Exception e) {
            logger.error("Error en endpoint listarSeccionesConCupo", e);
            throw e;
        }
    }
}
