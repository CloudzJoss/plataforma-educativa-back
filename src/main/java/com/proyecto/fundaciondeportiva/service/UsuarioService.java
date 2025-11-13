package com.proyecto.fundaciondeportiva.service;

import com.proyecto.fundaciondeportiva.dto.input.UsuarioInputDTO;
import com.proyecto.fundaciondeportiva.dto.output.UsuarioUpdateDTO;
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
import java.util.Optional;
import java.util.UUID;

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

    // --- Método de Creación MODIFICADO ---
    @Transactional
    public Usuario crearUsuario(UsuarioInputDTO inputDTO) {
        if (usuarioRepository.existsByEmail(inputDTO.getEmail())) {
            throw new RuntimeException("El correo electrónico ya está en uso.");
        }

        // **NUEVA VALIDACIÓN DNI**
        if (inputDTO.getRol() == Rol.ALUMNO && perfilAlumnoRepository.existsByDni(inputDTO.getDni())) {
            throw new RuntimeException("El DNI de alumno ya está registrado.");
        }
        if (inputDTO.getRol() == Rol.PROFESOR && perfilProfesorRepository.existsByDni(inputDTO.getDni())) {
            throw new RuntimeException("El DNI de profesor ya está registrado.");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(inputDTO.getNombre());
        nuevoUsuario.setEmail(inputDTO.getEmail());
        nuevoUsuario.setPassword(passwordEncoder.encode(inputDTO.getPassword()));
        nuevoUsuario.setRol(inputDTO.getRol());

        switch (inputDTO.getRol()) {
            case ALUMNO:
                // CAMBIO: Se valida 'grado'
                if (!StringUtils.hasText(inputDTO.getGrado())) {
                    throw new IllegalArgumentException("Para el rol ALUMNO, el grado es requerido.");
                }

                PerfilAlumno perfilAlumno = new PerfilAlumno();
                perfilAlumno.setGrado(inputDTO.getGrado()); // CAMBIO DE CARRERA A GRADO
                perfilAlumno.setDni(inputDTO.getDni()); // AÑADIDO DNI

                // --- LÓGICA DE GENERACIÓN DE CÓDIGO (Mantenida) ---
                String codigoGenerado;
                boolean codigoExiste;
                do {
                    codigoGenerado = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                    codigoExiste = perfilAlumnoRepository.existsByCodigoEstudiante(codigoGenerado);
                } while (codigoExiste);

                perfilAlumno.setCodigoEstudiante(codigoGenerado);
                nuevoUsuario.setPerfilAlumno(perfilAlumno);
                break;

            case PROFESOR:
                // ELIMINADA la validación de departamento
                PerfilProfesor perfilProfesor = new PerfilProfesor();
                perfilProfesor.setDni(inputDTO.getDni()); // AÑADIDO DNI
                // ELIMINADO: perfilProfesor.setDepartamento(...)
                nuevoUsuario.setPerfilProfesor(perfilProfesor);
                break;
            case ADMINISTRADOR:
                // No requiere perfil específico. El DNI no se guarda para el Admin de esta forma.
                break;
            default:
                throw new IllegalArgumentException("Rol no válido: " + inputDTO.getRol());
        }

        return usuarioRepository.save(nuevoUsuario);
    }

    // --- Listar todos los usuarios (sin cambios) ---
    public List<Usuario> listarTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    // --- Obtener usuario por ID (sin cambios) ---
    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el id: " + id));
    }

    public Optional<Usuario> obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // --- Editar Usuario MODIFICADO ---
    @Transactional
    public Usuario editarUsuario(Long id, UsuarioUpdateDTO updateDTO) {
        Usuario usuarioExistente = obtenerUsuarioPorId(id);

        // Actualización Campos Comunes
        if (StringUtils.hasText(updateDTO.getNombre())) {
            usuarioExistente.setNombre(updateDTO.getNombre());
        }
        if (StringUtils.hasText(updateDTO.getEmail()) && !updateDTO.getEmail().equals(usuarioExistente.getEmail())) {
            if (usuarioRepository.existsByEmail(updateDTO.getEmail())) {
                throw new RuntimeException("El nuevo correo electrónico ya está en uso.");
            }
            usuarioExistente.setEmail(updateDTO.getEmail());
        }
        if (StringUtils.hasText(updateDTO.getPassword())) {
            usuarioExistente.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }

        // Actualización Flexible de Perfiles
        if (usuarioExistente.getRol() == Rol.ALUMNO) {
            PerfilAlumno pa = usuarioExistente.getPerfilAlumno();
            // Lógica para inicializar perfil si es null...
            if (pa == null) {
                // Si el usuario es ALUMNO pero no tiene perfil, lo creamos
                pa = new PerfilAlumno();
                pa.setDni("PENDIENTE"); // Valor temporal para DNI
                pa.setCodigoEstudiante(UUID.randomUUID().toString().substring(0, 8).toUpperCase()); // Generar código temporal
                usuarioExistente.setPerfilAlumno(pa);
                perfilAlumnoRepository.save(pa);
            }

            // Actualización de DNI de Alumno
            if (StringUtils.hasText(updateDTO.getDni()) && !updateDTO.getDni().equals(pa.getDni())) {
                if (perfilAlumnoRepository.existsByDni(updateDTO.getDni())) {
                    throw new RuntimeException("El nuevo DNI de alumno ya está en uso.");
                }
                pa.setDni(updateDTO.getDni());
            }

            // Actualización de Grado de Alumno
            if (StringUtils.hasText(updateDTO.getGrado())) {
                pa.setGrado(updateDTO.getGrado()); // CAMBIO DE CARRERA A GRADO
            }

            // Si el código de estudiante viene, se actualiza
            if (StringUtils.hasText(updateDTO.getCodigoEstudiante())) {
                pa.setCodigoEstudiante(updateDTO.getCodigoEstudiante());
            }

        } else if (usuarioExistente.getRol() == Rol.PROFESOR) {
            PerfilProfesor pp = usuarioExistente.getPerfilProfesor();
            // Lógica para inicializar perfil si es null...
            if (pp == null) {
                // Si el usuario es PROFESOR pero no tiene perfil, lo creamos
                pp = new PerfilProfesor();
                pp.setDni("PENDIENTE"); // Valor temporal para DNI
                usuarioExistente.setPerfilProfesor(pp);
                perfilProfesorRepository.save(pp);
            }

            // Actualización de DNI de Profesor
            if (StringUtils.hasText(updateDTO.getDni()) && !updateDTO.getDni().equals(pp.getDni())) {
                if (perfilProfesorRepository.existsByDni(updateDTO.getDni())) {
                    throw new RuntimeException("El nuevo DNI de profesor ya está en uso.");
                }
                pp.setDni(updateDTO.getDni()); // AÑADIDO DNI
            }
            // ELIMINADA la lógica de actualización de 'departamento'
        }


        return usuarioRepository.save(usuarioExistente);
    }

    // --- Eliminar usuario (sin cambios) ---
    @Transactional
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con el id: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    // --- Método para Spring Security (sin cambios) ---
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .build();
    }
}