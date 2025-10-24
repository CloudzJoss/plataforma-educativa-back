package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.input.UsuarioInputDTO;
import com.proyecto.fundaciondeportiva.dto.input.UsuarioUpdateDTO; // Importa el DTO de actualización
import com.proyecto.fundaciondeportiva.dto.output.UsuarioOutputDTO;
import com.proyecto.fundaciondeportiva.model.Usuario;
import com.proyecto.fundaciondeportiva.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize; // Comentado por ahora
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios") // Ruta base para todos los endpoints de usuario
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService; // Inyección del servicio

    /**
     * Endpoint para crear un nuevo usuario (Alumno, Profesor o Admin).
     * Público por configuración en SecurityConfig.
     * Consume y produce JSON.
     *
     * @param inputDTO Datos del nuevo usuario.
     * @return ResponseEntity con el DTO del usuario creado y estado 201 Created.
     */
    @PostMapping(value = "/crear", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioOutputDTO> crearUsuario(@Valid @RequestBody UsuarioInputDTO inputDTO) {
        Usuario nuevoUsuario = usuarioService.crearUsuario(inputDTO);
        UsuarioOutputDTO outputDTO = convertirAUsuarioOutputDTO(nuevoUsuario);
        return new ResponseEntity<>(outputDTO, HttpStatus.CREATED);
    }

    /**
     * Endpoint para obtener la lista de todos los usuarios.
     * Temporalmente público por configuración en SecurityConfig.
     * Produce JSON.
     *
     * @return ResponseEntity con la lista de DTOs de usuarios y estado 200 OK.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    // @PreAuthorize("hasRole('ADMINISTRADOR')") // Seguridad comentada temporalmente
    public ResponseEntity<List<UsuarioOutputDTO>> listarTodosLosUsuarios() {
        List<Usuario> usuarios = usuarioService.listarTodosLosUsuarios();
        List<UsuarioOutputDTO> outputDTOs = usuarios.stream()
                .map(this::convertirAUsuarioOutputDTO) // Usa el método de mapeo
                .collect(Collectors.toList());
        return ResponseEntity.ok(outputDTOs);
    }

    /**
     * Endpoint para obtener un usuario específico por su ID.
     * Temporalmente público por configuración en SecurityConfig.
     * Produce JSON.
     *
     * @param id El ID del usuario a buscar.
     * @return ResponseEntity con el DTO del usuario encontrado y estado 200 OK.
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    // @PreAuthorize("hasRole('ADMINISTRADOR')") // Seguridad comentada temporalmente
    public ResponseEntity<UsuarioOutputDTO> obtenerUsuarioPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
        UsuarioOutputDTO outputDTO = convertirAUsuarioOutputDTO(usuario);
        return ResponseEntity.ok(outputDTO);
    }

    /**
     * Endpoint para editar un usuario existente por su ID.
     * Temporalmente público por configuración en SecurityConfig.
     * Consume y produce JSON. Usa UsuarioUpdateDTO para permitir campos opcionales.
     *
     * @param id El ID del usuario a editar.
     * @param updateDTO Datos a actualizar (campos opcionales).
     * @return ResponseEntity con el DTO del usuario actualizado y estado 200 OK.
     */
    @PutMapping(value = "/editar/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    // @PreAuthorize("hasRole('ADMINISTRADOR')") // Seguridad comentada temporalmente
    public ResponseEntity<UsuarioOutputDTO> editarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO updateDTO) {
        Usuario usuarioActualizado = usuarioService.editarUsuario(id, updateDTO); // Llama al servicio con el DTO correcto
        UsuarioOutputDTO outputDTO = convertirAUsuarioOutputDTO(usuarioActualizado);
        return ResponseEntity.ok(outputDTO);
    }

    /**
     * Endpoint para eliminar un usuario por su ID.
     * Temporalmente público por configuración en SecurityConfig.
     *
     * @param id El ID del usuario a eliminar.
     * @return ResponseEntity con estado 204 No Content si la eliminación fue exitosa.
     */
    @DeleteMapping("/eliminar/{id}")
    // @PreAuthorize("hasRole('ADMINISTRADOR')") // Seguridad comentada temporalmente
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build(); // Respuesta HTTP 204 indica éxito sin contenido
    }


    /**
     * Método privado de utilidad para convertir una entidad Usuario a UsuarioOutputDTO.
     * Se encarga de mapear los campos comunes y aplanar los datos de los perfiles.
     * Excluye datos sensibles como la contraseña.
     *
     * @param usuario La entidad Usuario a convertir.
     * @return El objeto UsuarioOutputDTO mapeado.
     */
    private UsuarioOutputDTO convertirAUsuarioOutputDTO(Usuario usuario) {
        // Validación para evitar NullPointerException si el usuario es null
        if (usuario == null) {
            return null;
        }

        UsuarioOutputDTO dto = new UsuarioOutputDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setRol(usuario.getRol());

        // Mapea datos del perfil de alumno si existe
        if (usuario.getPerfilAlumno() != null) {
            dto.setCarrera(usuario.getPerfilAlumno().getCarrera());
            dto.setCodigoEstudiante(usuario.getPerfilAlumno().getCodigoEstudiante());
        }
        // Mapea datos del perfil de profesor si existe
        if (usuario.getPerfilProfesor() != null) {
            dto.setDepartamento(usuario.getPerfilProfesor().getDepartamento());
        }
        return dto;
    }
}

