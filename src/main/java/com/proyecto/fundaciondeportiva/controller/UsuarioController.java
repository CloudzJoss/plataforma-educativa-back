package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.input.UsuarioInputDTO;
import com.proyecto.fundaciondeportiva.dto.output.UsuarioUpdateDTO;
import com.proyecto.fundaciondeportiva.dto.output.UsuarioOutputDTO;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Endpoint para crear un nuevo usuario.
     */
    @PostMapping(value = "/crear", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')") // ✅ CORREGIDO: Rol correcto
    public ResponseEntity<UsuarioOutputDTO> crearUsuario(@Valid @RequestBody UsuarioInputDTO inputDTO) {
        Usuario nuevoUsuario = usuarioService.crearUsuario(inputDTO);
        UsuarioOutputDTO outputDTO = UsuarioOutputDTO.deEntidad(nuevoUsuario);
        return new ResponseEntity<>(outputDTO, HttpStatus.CREATED);
    }

    /**
     * Endpoint para obtener el propio perfil.
     */
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioOutputDTO> obtenerPerfilPropio() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String emailAutenticado = userDetails.getUsername();

        Usuario usuario = usuarioService.obtenerUsuarioPorEmail(emailAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado."));

        UsuarioOutputDTO outputDTO = UsuarioOutputDTO.deEntidad(usuario);
        return ResponseEntity.ok(outputDTO);
    }

    // --- Endpoints de Gestión (Solo ADMINISTRADOR) ---

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')") // ✅ CORREGIDO
    public ResponseEntity<List<UsuarioOutputDTO>> listarTodosLosUsuarios() {
        List<Usuario> usuarios = usuarioService.listarTodosLosUsuarios();
        List<UsuarioOutputDTO> outputDTOs = usuarios.stream()
                .map(UsuarioOutputDTO::deEntidad)
                .collect(Collectors.toList());
        return ResponseEntity.ok(outputDTOs);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')") // ✅ CORREGIDO
    public ResponseEntity<UsuarioOutputDTO> obtenerUsuarioPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
        UsuarioOutputDTO outputDTO = UsuarioOutputDTO.deEntidad(usuario);
        return ResponseEntity.ok(outputDTO);
    }

    @PutMapping(value = "/editar/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')") // ✅ CORREGIDO
    public ResponseEntity<UsuarioOutputDTO> editarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO updateDTO) {
        Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, updateDTO);
        UsuarioOutputDTO outputDTO = UsuarioOutputDTO.deEntidad(usuarioActualizado);
        return ResponseEntity.ok(outputDTO);
    }

    @DeleteMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')") // ✅ CORREGIDO
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}