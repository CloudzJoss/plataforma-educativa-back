package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.request.SeccionRequestDTO;
import com.proyecto.fundaciondeportiva.dto.response.SeccionResponseDTO;
import com.proyecto.fundaciondeportiva.dto.response.UsuarioResponse;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
// ❌ IMPORT DE TURNO ELIMINADO
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

    @GetMapping("/mis-secciones")
    @PreAuthorize("hasRole('PROFESOR')")
    public ResponseEntity<List<SeccionResponseDTO>> obtenerMisSecciones() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String emailProfesor = auth.getName();

            // Usamos el método que devuelve el DTO plano (UsuarioResponse)
            UsuarioResponse usuarioDTO = servicioUsuario.obtenerUsuarioResponsePorEmail(emailProfesor);

            // Verificamos con los getters del DTO plano
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

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<SeccionResponseDTO> crearSeccion(@Valid @RequestBody SeccionRequestDTO request) {
        SeccionResponseDTO seccion = servicioSeccion.crearSeccion(request);
        return new ResponseEntity<>(seccion, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<SeccionResponseDTO> actualizarSeccion(@PathVariable Long id, @Valid @RequestBody SeccionRequestDTO request) {
        return ResponseEntity.ok(servicioSeccion.actualizarSeccion(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarSeccion(@PathVariable Long id) {
        servicioSeccion.eliminarSeccion(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> desactivarSeccion(@PathVariable Long id) {
        servicioSeccion.desactivarSeccion(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> activarSeccion(@PathVariable Long id) {
        servicioSeccion.activarSeccion(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeccionResponseDTO>> listarTodasLasSecciones() {
        return ResponseEntity.ok(servicioSeccion.listarTodasLasSecciones());
    }

    @GetMapping("/activas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeccionResponseDTO>> listarSeccionesActivas() {
        return ResponseEntity.ok(servicioSeccion.listarSeccionesActivas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SeccionResponseDTO> obtenerSeccionPorId(@PathVariable Long id) {
        return ResponseEntity.ok(servicioSeccion.obtenerSeccionPorId(id));
    }

    @GetMapping("/curso/{cursoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeccionResponseDTO>> listarSeccionesPorCurso(@PathVariable Long cursoId) {
        return ResponseEntity.ok(servicioSeccion.listarSeccionesPorCurso(cursoId));
    }

    @GetMapping("/profesor/{profesorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeccionResponseDTO>> listarSeccionesPorProfesor(@PathVariable Long profesorId) {
        return ResponseEntity.ok(servicioSeccion.listarSeccionesPorProfesor(profesorId));
    }

    // ❌ ELIMINADO: listarSeccionesPorTurno

    @GetMapping("/nivel/{nivel}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeccionResponseDTO>> listarSeccionesPorNivel(@PathVariable NivelAcademico nivel) {
        return ResponseEntity.ok(servicioSeccion.listarSeccionesPorNivel(nivel));
    }

    @GetMapping("/con-cupo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SeccionResponseDTO>> listarSeccionesConCupo() {
        return ResponseEntity.ok(servicioSeccion.listarSeccionesConCupo());
    }
}