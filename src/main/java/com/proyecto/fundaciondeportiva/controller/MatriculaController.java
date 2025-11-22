package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.request.MatriculaRequestDTO;
import com.proyecto.fundaciondeportiva.dto.response.MatriculaResponseDTO;
import com.proyecto.fundaciondeportiva.dto.response.UsuarioResponse;
import com.proyecto.fundaciondeportiva.model.enums.EstadoMatricula;
import com.proyecto.fundaciondeportiva.service.ServicioMatricula;
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
 * Controlador REST para la gestión de Matrículas
 */
@RestController
@RequestMapping("/api/matriculas")
public class MatriculaController {

    private static final Logger logger = LoggerFactory.getLogger(MatriculaController.class);

    @Autowired
    private ServicioMatricula servicioMatricula;

    @Autowired
    private UsuarioService servicioUsuario;

    // --- ENDPOINTS DE ALUMNO ---

    /**
     * El alumno se matricula en una sección
     * POST /api/matriculas/matricularse
     */
    @PostMapping("/matricularse")
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<MatriculaResponseDTO> matricularseEnSeccion(@Valid @RequestBody MatriculaRequestDTO request) {
        try {
            // Obtener el ID del alumno autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String emailAlumno = auth.getName();

            logger.info("Alumno {} solicita matricularse en sección ID {}", emailAlumno, request.getSeccionId());

            // Obtener el usuario completo por email
            UsuarioResponse usuarioDTO = servicioUsuario.obtenerUsuarioResponsePorEmail(emailAlumno);
            Long alumnoId = usuarioDTO.getId();

            // Procesar la matrícula
            MatriculaResponseDTO matriculaCreada = servicioMatricula.matricularseEnSeccion(alumnoId, request);

            return new ResponseEntity<>(matriculaCreada, HttpStatus.CREATED);

        } catch (Exception e) {
            logger.error("Error en endpoint matricularseEnSeccion", e);
            throw e;
        }
    }

    /**
     * El alumno se retira de una sección
     * DELETE /api/matriculas/retirarse/{seccionId}
     */
    @DeleteMapping("/retirarse/{seccionId}")
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<MatriculaResponseDTO> retirarseDeSeccion(@PathVariable Long seccionId) {
        try {
            // Obtener el ID del alumno autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String emailAlumno = auth.getName();

            logger.info("Alumno {} solicita retirarse de sección ID {}", emailAlumno, seccionId);

            UsuarioResponse usuarioDTO = servicioUsuario.obtenerUsuarioResponsePorEmail(emailAlumno);
            Long alumnoId = usuarioDTO.getId();

            MatriculaResponseDTO matriculaActualizada = servicioMatricula.retirarseDeSeccion(alumnoId, seccionId);

            return ResponseEntity.ok(matriculaActualizada);

        } catch (Exception e) {
            logger.error("Error en endpoint retirarseDeSeccion", e);
            throw e;
        }
    }

    /**
     * El alumno ve todas sus matrículas
     * GET /api/matriculas/mis-matriculas
     */
    @GetMapping("/mis-matriculas")
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<List<MatriculaResponseDTO>> verMisMatriculas() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String emailAlumno = auth.getName();

            logger.info("Alumno {} consulta sus matrículas", emailAlumno);

            UsuarioResponse usuarioDTO = servicioUsuario.obtenerUsuarioResponsePorEmail(emailAlumno);
            Long alumnoId = usuarioDTO.getId();

            List<MatriculaResponseDTO> matriculas = servicioMatricula.listarMisMatriculas(alumnoId);

            return ResponseEntity.ok(matriculas);

        } catch (Exception e) {
            logger.error("Error en endpoint verMisMatriculas", e);
            throw e;
        }
    }

    /**
     * El alumno ve solo sus matrículas activas
     * GET /api/matriculas/mis-matriculas/activas
     */
    @GetMapping("/mis-matriculas/activas")
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<List<MatriculaResponseDTO>> verMisMatriculasActivas() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String emailAlumno = auth.getName();

            logger.info("Alumno {} consulta sus matrículas activas", emailAlumno);

            UsuarioResponse usuarioDTO = servicioUsuario.obtenerUsuarioResponsePorEmail(emailAlumno);
            Long alumnoId = usuarioDTO.getId();

            List<MatriculaResponseDTO> matriculas = servicioMatricula.listarMisMatriculasActivas(alumnoId);

            return ResponseEntity.ok(matriculas);

        } catch (Exception e) {
            logger.error("Error en endpoint verMisMatriculasActivas", e);
            throw e;
        }
    }

    // --- ENDPOINTS DE PROFESOR ---

    /**
     * El profesor ve todos los alumnos de una sección
     * GET /api/matriculas/seccion/{seccionId}/alumnos
     */
    @GetMapping("/seccion/{seccionId}/alumnos")
    @PreAuthorize("hasRole('PROFESOR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<MatriculaResponseDTO>> listarAlumnosDeSeccion(@PathVariable Long seccionId) {
        try {
            logger.info("Consultando alumnos de la sección ID: {}", seccionId);

            List<MatriculaResponseDTO> matriculas = servicioMatricula.listarAlumnosDeSeccion(seccionId);

            return ResponseEntity.ok(matriculas);

        } catch (Exception e) {
            logger.error("Error en endpoint listarAlumnosDeSeccion", e);
            throw e;
        }
    }

    /**
     * El profesor ve solo los alumnos activos de una sección
     * GET /api/matriculas/seccion/{seccionId}/alumnos/activos
     */
    @GetMapping("/seccion/{seccionId}/alumnos/activos")
    @PreAuthorize("hasRole('PROFESOR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<MatriculaResponseDTO>> listarAlumnosActivosDeSeccion(@PathVariable Long seccionId) {
        try {
            logger.info("Consultando alumnos activos de la sección ID: {}", seccionId);

            List<MatriculaResponseDTO> matriculas = servicioMatricula.listarAlumnosActivosDeSeccion(seccionId);

            return ResponseEntity.ok(matriculas);

        } catch (Exception e) {
            logger.error("Error en endpoint listarAlumnosActivosDeSeccion", e);
            throw e;
        }
    }

    /**
     * Asignar calificación a un alumno
     * PATCH /api/matriculas/{id}/calificacion
     */
    @PatchMapping("/{id}/calificacion")
    @PreAuthorize("hasRole('PROFESOR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<MatriculaResponseDTO> asignarCalificacion(
            @PathVariable Long id,
            @RequestParam Double calificacion) {
        try {
            logger.info("Asignando calificación {} a matrícula ID {}", calificacion, id);

            MatriculaResponseDTO matriculaActualizada = servicioMatricula.asignarCalificacion(id, calificacion);

            return ResponseEntity.ok(matriculaActualizada);

        } catch (Exception e) {
            logger.error("Error en endpoint asignarCalificacion", e);
            throw e;
        }
    }

    // --- ENDPOINTS DE ADMINISTRADOR ---

    /**
     * Listar todas las matrículas del sistema
     * GET /api/matriculas
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<MatriculaResponseDTO>> listarTodasLasMatriculas() {
        try {
            logger.info("Listando todas las matrículas del sistema");

            List<MatriculaResponseDTO> matriculas = servicioMatricula.listarTodasLasMatriculas();

            return ResponseEntity.ok(matriculas);

        } catch (Exception e) {
            logger.error("Error en endpoint listarTodasLasMatriculas", e);
            throw e;
        }
    }

    /**
     * Obtener una matrícula específica por ID
     * GET /api/matriculas/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MatriculaResponseDTO> obtenerMatriculaPorId(@PathVariable Long id) {
        try {
            logger.info("Obteniendo matrícula ID: {}", id);

            MatriculaResponseDTO matricula = servicioMatricula.obtenerMatriculaPorId(id);

            return ResponseEntity.ok(matricula);

        } catch (Exception e) {
            logger.error("Error en endpoint obtenerMatriculaPorId", e);
            throw e;
        }
    }

    /**
     * Actualizar el estado de una matrícula
     * PATCH /api/matriculas/{id}/estado
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MatriculaResponseDTO> actualizarEstadoMatricula(
            @PathVariable Long id,
            @RequestParam EstadoMatricula estado) {
        try {
            logger.info("Actualizando estado de matrícula ID {} a {}", id, estado);

            MatriculaResponseDTO matriculaActualizada = servicioMatricula.actualizarEstadoMatricula(id, estado);

            return ResponseEntity.ok(matriculaActualizada);

        } catch (Exception e) {
            logger.error("Error en endpoint actualizarEstadoMatricula", e);
            throw e;
        }
    }

    /**
     * Eliminar una matrícula
     * DELETE /api/matriculas/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarMatricula(@PathVariable Long id) {
        try {
            logger.info("Eliminando matrícula ID: {}", id);

            servicioMatricula.eliminarMatricula(id);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            logger.error("Error en endpoint eliminarMatricula", e);
            throw e;
        }
    }
}