package com.proyecto.fundaciondeportiva.service;

import com.proyecto.fundaciondeportiva.dto.input.UsuarioInputDTO;
import com.proyecto.fundaciondeportiva.model.PerfilAlumno;
import com.proyecto.fundaciondeportiva.model.PerfilProfesor;
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

// Añade "implements UserDetailsService" aquí
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

    @Transactional
    public Usuario crearUsuario(UsuarioInputDTO inputDTO) {
        // ... tu lógica para crear usuario se queda exactamente igual ...
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
                if (inputDTO.getCarrera() == null || inputDTO.getCodigoEstudiante() == null) {
                    throw new IllegalArgumentException("Para el rol ALUMNO, la carrera y el código son requeridos.");
                }
                PerfilAlumno perfilAlumno = new PerfilAlumno();
                perfilAlumno.setCarrera(inputDTO.getCarrera());
                perfilAlumno.setCodigoEstudiante(inputDTO.getCodigoEstudiante());
                perfilAlumnoRepository.save(perfilAlumno);
                nuevoUsuario.setPerfilAlumno(perfilAlumno);
                break;
            case PROFESOR:
                if (inputDTO.getDepartamento() == null) {
                    throw new IllegalArgumentException("Para el rol PROFESOR, el departamento es requerido.");
                }
                PerfilProfesor perfilProfesor = new PerfilProfesor();
                perfilProfesor.setDepartamento(inputDTO.getDepartamento());
                perfilProfesorRepository.save(perfilProfesor);
                nuevoUsuario.setPerfilProfesor(perfilProfesor);
                break;
            case ADMINISTRADOR:
                break;
            default:
                throw new IllegalArgumentException("Rol no válido: " + inputDTO.getRol());
        }

        return usuarioRepository.save(nuevoUsuario);
    }

    // --- ESTE ES EL MÉTODO NUEVO Y OBLIGATORIO ---
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscamos al usuario por su email, que es nuestro "username"
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + username));

        // Creamos un objeto UserDetails que Spring Security entiende
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name()) // Convierte el Enum Rol a un String
                .build();
    }
}