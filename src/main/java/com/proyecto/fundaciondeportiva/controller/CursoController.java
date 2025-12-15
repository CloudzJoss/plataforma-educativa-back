package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.request.CursoRequestDTO;
import com.proyecto.fundaciondeportiva.dto.response.CursoResponseDTO;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import com.proyecto.fundaciondeportiva.service.ServicioCurso;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador API para la gestión de Cursos.
 * 🚨 ACTUALIZADO: Se corrigió @PreAuthorize para usar 'ADMINISTRADOR'
 */
@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    @Autowired
    private ServicioCurso servicioCurso;

    // --- Endpoints de ADMIN ---

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CursoResponseDTO> crearCurso(@Valid @RequestBody CursoRequestDTO request) {
        String emailAdmin = SecurityContextHolder.getContext().getAuthentication().getName();
        CursoResponseDTO cursoCreado = servicioCurso.crearCurso(request, emailAdmin);
        return new ResponseEntity<>(cursoCreado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')") // 🚨 CORRECCIÓN
    public ResponseEntity<CursoResponseDTO> actualizarCurso(@PathVariable Long id, @Valid @RequestBody CursoRequestDTO request) {
        CursoResponseDTO cursoActualizado = servicioCurso.actualizarCurso(id, request);
        return ResponseEntity.ok(cursoActualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')") // 🚨 CORRECCIÓN
    public ResponseEntity<Void> eliminarCurso(@PathVariable Long id) {
        servicioCurso.eliminarCurso(id);
        return ResponseEntity.noContent().build();
    }

    // --- Endpoints Públicos (para usuarios logueados) ---

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CursoResponseDTO>> listarTodosLosCursos() {
        List<CursoResponseDTO> cursos = servicioCurso.listarTodosLosCursos();
        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CursoResponseDTO> obtenerCursoPorId(@PathVariable Long id) {
        CursoResponseDTO curso = servicioCurso.obtenerCursoPorId(id);
        return ResponseEntity.ok(curso);
    }

    /**
     * Endpoint de búsqueda actualizada, ej: /api/cursos/buscar?nivel=SECUNDARIA
     */
    @GetMapping("/buscar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CursoResponseDTO>> buscarCursosPorNivel(
            @RequestParam NivelAcademico nivel) {
        List<CursoResponseDTO> cursos = servicioCurso.listarCursosPorNivel(nivel);
        return ResponseEntity.ok(cursos);
    }
}