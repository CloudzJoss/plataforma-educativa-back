package com.proyecto.fundaciondeportiva.service;

import com.proyecto.fundaciondeportiva.dto.input.UsuarioInputDTO;
import com.proyecto.fundaciondeportiva.dto.input.UsuarioUpdateDTO; // Asegúrate que este import esté
import com.proyecto.fundaciondeportiva.model.PerfilAlumno;
import com.proyecto.fundaciondeportiva.model.PerfilProfesor;
import com.proyecto.fundaciondeportiva.model.Rol;
import com.proyecto.fundaciondeportiva.model.Usuario;
import com.proyecto.fundaciondeportiva.repository.PerfilAlumnoRepository;
import com.proyecto.fundaciondeportiva.repository.PerfilProfesorRepository;
import com.proyecto.fundaciondeportiva.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional; // Asegúrate de tener este import

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilAlumnoRepository perfilAlumnoRepository;

    @Autowired
    private PerfilProfesorRepository perfilProfesorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- Método de Creación (Sin cambios) ---
    @Transactional
    public Usuario crearUsuario(UsuarioInputDTO inputDTO) {
        if (usuarioRepository.existsByEmail(inputDTO.getEmail())) {
            throw new RuntimeException("El correo electrónico ya está en uso.");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(inputDTO.getNombre());
        nuevoUsuario.setEmail(inputDTO.getEmail());
        nuevoUsuario.setPassword(passwordEncoder.encode(inputDTO.getPassword()));
        nuevoUsuario.setRol(inputDTO.getRol());

        switch (inputDTO.getRol()) {
            case ALUMNO:
                if (!StringUtils.hasText(inputDTO.getCarrera()) || !StringUtils.hasText(inputDTO.getCodigoEstudiante())) {
                    throw new IllegalArgumentException("Para el rol ALUMNO, la carrera y el código son requeridos.");
                }
                PerfilAlumno perfilAlumno = new PerfilAlumno();
                perfilAlumno.setCarrera(inputDTO.getCarrera());
                perfilAlumno.setCodigoEstudiante(inputDTO.getCodigoEstudiante());
                nuevoUsuario.setPerfilAlumno(perfilAlumno);
                break;
            case PROFESOR:
                if (!StringUtils.hasText(inputDTO.getDepartamento())) {
                    throw new IllegalArgumentException("Para el rol PROFESOR, el departamento es requerido.");
                }
                PerfilProfesor perfilProfesor = new PerfilProfesor();
                perfilProfesor.setDepartamento(inputDTO.getDepartamento());
                nuevoUsuario.setPerfilProfesor(perfilProfesor);
                break;
            case ADMINISTRADOR:
                break;
            default:
                throw new IllegalArgumentException("Rol no válido: " + inputDTO.getRol());
        }

        return usuarioRepository.save(nuevoUsuario);
    }

    // --- Listar todos los usuarios ---
    public List<Usuario> listarTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    // --- Obtener usuario por ID ---
    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el id: " + id));
    }

    // --- CORRECCIÓN AQUÍ: Cambia UsuarioInputDTO por UsuarioUpdateDTO ---
    @Transactional
    public Usuario editarUsuario(Long id, UsuarioUpdateDTO updateDTO) {
        Usuario usuarioExistente = obtenerUsuarioPorId(id);

        // --- Actualización de Campos Comunes ---
        if (StringUtils.hasText(updateDTO.getNombre())) {
            usuarioExistente.setNombre(updateDTO.getNombre());
        }
        if (StringUtils.hasText(updateDTO.getEmail()) && !updateDTO.getEmail().equals(usuarioExistente.getEmail())) {
            if (usuarioRepository.existsByEmail(updateDTO.getEmail())) {
                throw new RuntimeException("El nuevo correo electrónico ya está en uso por otro usuario.");
            }
            usuarioExistente.setEmail(updateDTO.getEmail());
        }
        if (StringUtils.hasText(updateDTO.getPassword())) {
            usuarioExistente.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }
        // --- FIN Actualización de Campos Comunes ---


        // --- Actualización de Perfiles (FLEXIBLE: Actualiza o crea sin importar el rol) ---
        // ADVERTENCIA: Esto puede llevar a datos inconsistentes (ej. un Admin con carrera).

        // Intenta actualizar/crear Perfil Alumno si vienen datos de alumno en el DTO
        if (StringUtils.hasText(updateDTO.getCarrera()) || StringUtils.hasText(updateDTO.getCodigoEstudiante())) {
            PerfilAlumno pa = usuarioExistente.getPerfilAlumno();
            if (pa == null) {
                // Si no tiene perfil de alumno, crea uno nuevo
                pa = new PerfilAlumno();
                usuarioExistente.setPerfilAlumno(pa);
                // Guarda el perfil explícitamente para asegurar que tenga ID antes de guardar el usuario
                // (Depende de la configuración de cascada, pero es más seguro)
                perfilAlumnoRepository.save(pa);
            }
            // Actualiza los campos del perfil si se proporcionaron en el DTO
            if (StringUtils.hasText(updateDTO.getCarrera())) {
                pa.setCarrera(updateDTO.getCarrera());
            }
            if (StringUtils.hasText(updateDTO.getCodigoEstudiante())) {
                pa.setCodigoEstudiante(updateDTO.getCodigoEstudiante());
            }
        }

        // Intenta actualizar/crear Perfil Profesor si vienen datos de profesor en el DTO
        if (StringUtils.hasText(updateDTO.getDepartamento())) {
            PerfilProfesor pp = usuarioExistente.getPerfilProfesor();
            if (pp == null) {
                // Si no tiene perfil de profesor, crea uno nuevo
                pp = new PerfilProfesor();
                usuarioExistente.setPerfilProfesor(pp);
                // Guarda el perfil explícitamente
                perfilProfesorRepository.save(pp);
            }
            // Actualiza el departamento si se proporcionó en el DTO
            pp.setDepartamento(updateDTO.getDepartamento());
        }

        // --- FIN Actualización de Perfiles ---

        // Guarda el usuario. JPA/Hibernate gestionará las actualizaciones de los perfiles asociados
        // gracias a CascadeType.ALL y a que guardamos explícitamente los nuevos perfiles.
        return usuarioRepository.save(usuarioExistente);
    }

    // --- Eliminar usuario ---
    @Transactional
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con el id: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    // --- Método para Spring Security ---
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + username));

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .build();
    }
}