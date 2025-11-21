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
     * Endpoint para crear un nuevo usuario (Alumno, Profesor o Admin).
     * 🚨 CORREGIDO: Llama a 'UsuarioOutputDTO.deEntidad'
     */
    @PostMapping(value = "/crear", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    // 🚨 NOTA: Tu Enum de Rol es 'ADMIN', no 'ADMINISTRADOR'
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioOutputDTO> crearUsuario(@Valid @RequestBody UsuarioInputDTO inputDTO) {
        // El servicio devuelve la entidad
        Usuario nuevoUsuario = usuarioService.crearUsuario(inputDTO);

        // 🚨 CAMBIO: Usamos el método de fábrica del DTO para convertir
        UsuarioOutputDTO outputDTO = UsuarioOutputDTO.deEntidad(nuevoUsuario);
        return new ResponseEntity<>(outputDTO, HttpStatus.CREATED);
    }

    /**
     * Endpoint para obtener el propio perfil.
     * 🚨 CORREGIDO: Llama a 'UsuarioOutputDTO.deEntidad'
     */
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioOutputDTO> obtenerPerfilPropio() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String emailAutenticado = userDetails.getUsername();

        Usuario usuario = usuarioService.obtenerUsuarioPorEmail(emailAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en la base de datos."));

        // 🚨 CAMBIO: Usamos el método de fábrica del DTO
        UsuarioOutputDTO outputDTO = UsuarioOutputDTO.deEntidad(usuario);
        return ResponseEntity.ok(outputDTO);
    }

    // --- Endpoints de Gestión (Solo ADMINISTRADOR) ---

    /**
     * Endpoint para listar todos los usuarios.
     * 🚨 CORREGIDO: Llama a 'UsuarioOutputDTO.deEntidad'
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')") // 🚨 NOTA: Tu Enum de Rol es 'ADMIN'
    public ResponseEntity<List<UsuarioOutputDTO>> listarTodosLosUsuarios() {
        List<Usuario> usuarios = usuarioService.listarTodosLosUsuarios();

        // 🚨 CAMBIO: Mapeo limpio usando el método de fábrica
        List<UsuarioOutputDTO> outputDTOs = usuarios.stream()
                .map(UsuarioOutputDTO::deEntidad) // Equivale a usuario -> UsuarioOutputDTO.deEntidad(usuario)
                .collect(Collectors.toList());
        return ResponseEntity.ok(outputDTOs);
    }

    /**
     * Endpoint para obtener un usuario por ID.
     * 🚨 CORREGIDO: Llama a 'UsuarioOutputDTO.deEntidad'
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')") // 🚨 NOTA: Tu Enum de Rol es 'ADMIN'
    public ResponseEntity<UsuarioOutputDTO> obtenerUsuarioPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id);

        // 🚨 CAMBIO: Usamos el método de fábrica del DTO
        UsuarioOutputDTO outputDTO = UsuarioOutputDTO.deEntidad(usuario);
        return ResponseEntity.ok(outputDTO);
    }

    /**
     * Endpoint para editar un usuario.
     * 🚨 CORREGIDO: Llama a 'UsuarioOutputDTO.deEntidad'
     */
    @PutMapping(value = "/editar/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')") // 🚨 NOTA: Tu Enum de Rol es 'ADMIN'
    public ResponseEntity<UsuarioOutputDTO> editarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO updateDTO) {
        Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, updateDTO);

        // 🚨 CAMBIO: Usamos el método de fábrica del DTO
        UsuarioOutputDTO outputDTO = UsuarioOutputDTO.deEntidad(usuarioActualizado);
        return ResponseEntity.ok(outputDTO);
    }

    @DeleteMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')") // 🚨 NOTA: Tu Enum de Rol es 'ADMIN'
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }


    /**
     * 🚨 ¡MÉTODO ELIMINADO!
     * Este método privado (convertirAUsuarioOutputDTO) causaba el error 500
     * porque intentaba llamar a 'dto.setDni()', que ya no existe.
     * La lógica de conversión ahora vive de forma estática en 'UsuarioOutputDTO.deEntidad()'.
     */
    /*
    private UsuarioOutputDTO convertirAUsuarioOutputDTO(Usuario usuario) {
        // ... (TODO ESTE CÓDIGO ANTIGUO Y ROTO SE HA IDO)
    }
    */
}