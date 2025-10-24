package com.proyecto.fundaciondeportiva.controller;


import com.proyecto.fundaciondeportiva.dto.input.UsuarioInputDTO;
import com.proyecto.fundaciondeportiva.dto.output.UsuarioOutputDTO;
import com.proyecto.fundaciondeportiva.model.Usuario;
import com.proyecto.fundaciondeportiva.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;
    //---------------------------------INICIO------------------------------------------------


    //---------------------------------ADMINISTRADOR------------------------------------------------
    // Endpoint para que el admin cree un nuevo usuario (profesor, alumno, etc.)
    @PostMapping("/crear")
    public ResponseEntity<UsuarioOutputDTO> crearUsuario(@Valid @RequestBody UsuarioInputDTO inputDTO) {
        // 1. Recibe el DTO de entrada y se lo pasa al servicio
        Usuario nuevoUsuario = usuarioService.crearUsuario(inputDTO);

        // 2. Convierte el modelo guardado a un DTO de salida
        UsuarioOutputDTO outputDTO = convertirAMapper(nuevoUsuario);

        // 3. Devuelve una respuesta HTTP 201 (Created) con el DTO de salida
        return new ResponseEntity<>(outputDTO, HttpStatus.CREATED);
    }

    //---------------------------------ALUMNO------------------------------------------------
    //---------------------------------PROFESOR------------------------------------------------

    // Método privado para mapear de Entidad a DTO de salida
    private UsuarioOutputDTO convertirAMapper(Usuario usuario) {
        UsuarioOutputDTO dto = new UsuarioOutputDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setRol(usuario.getRol());

        // Aplanar los datos del perfil si existen
        if (usuario.getPerfilAlumno() != null) {
            dto.setCarrera(usuario.getPerfilAlumno().getCarrera());
            dto.setCodigoEstudiante(usuario.getPerfilAlumno().getCodigoEstudiante());
        }
        if (usuario.getPerfilProfesor() != null) {
            dto.setDepartamento(usuario.getPerfilProfesor().getDepartamento());
        }

        return dto;
    }
}
