package com.proyecto.fundaciondeportiva.service;

import com.proyecto.fundaciondeportiva.dto.input.UsuarioInputDTO;
import com.proyecto.fundaciondeportiva.dto.output.UsuarioUpdateDTO;
import com.proyecto.fundaciondeportiva.dto.response.UsuarioResponse;
import com.proyecto.fundaciondeportiva.exception.RecursoNoEncontradoException;
import com.proyecto.fundaciondeportiva.exception.ValidacionException;
import com.proyecto.fundaciondeportiva.model.entity.PerfilAlumno;
import com.proyecto.fundaciondeportiva.model.entity.PerfilProfesor;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import com.proyecto.fundaciondeportiva.repository.PerfilAlumnoRepository;
import com.proyecto.fundaciondeportiva.repository.PerfilProfesorRepository;
import com.proyecto.fundaciondeportiva.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
    }

    @Transactional
    public Usuario crearUsuario(UsuarioInputDTO request) {

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ValidacionException("El correo electrónico ya está en uso.");
        }

        //  CAMBIO: Mapeo de Nombres y Apellidos
        Usuario nuevoUsuario = Usuario.builder()
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(request.getRol())
                .build();

        if (request.getRol() == Rol.ALUMNO) {
            if (request.getDniAlumno() == null || request.getDniAlumno().isBlank()) {
                throw new ValidacionException("El DNI es obligatorio para el alumno.");
            }
            if (perfilAlumnoRepository.existsByDni(request.getDniAlumno())) {
                throw new ValidacionException("El DNI del alumno ya está registrado.");
            }
            if (request.getNivel() == null) {
                throw new ValidacionException("El Nivel Académico es obligatorio.");
            }
            if (request.getGrado() == null || request.getGrado().isBlank()) {
                throw new ValidacionException("El Grado es obligatorio.");
            }

            String codigoEstudiante = request.getCodigoEstudiante();
            if (codigoEstudiante == null || codigoEstudiante.isBlank()) {
                codigoEstudiante = generarCodigoEstudianteUnico();
            } else if (perfilAlumnoRepository.existsByCodigoEstudiante(codigoEstudiante)) {
                throw new ValidacionException("El código de estudiante ya existe.");
            }

            PerfilAlumno perfil = PerfilAlumno.builder()
                    .dni(request.getDniAlumno())
                    .nivel(request.getNivel())
                    .grado(request.getGrado())
                    .codigoEstudiante(codigoEstudiante)
                    .usuario(nuevoUsuario)
                    .build();
            nuevoUsuario.setPerfilAlumno(perfil);

        } else if (request.getRol() == Rol.PROFESOR) {
            if (request.getDniProfesor() == null || request.getDniProfesor().isBlank()) {
                throw new ValidacionException("El DNI es obligatorio para el profesor.");
            }
            if (perfilProfesorRepository.existsByDni(request.getDniProfesor())) {
                throw new ValidacionException("El DNI del profesor ya está registrado.");
            }

            PerfilProfesor perfil = PerfilProfesor.builder()
                    .dni(request.getDniProfesor())
                    .telefono(request.getTelefono())
                    .experiencia(request.getExperiencia())
                    .gradoAcademico(request.getGradoAcademico())
                    .usuario(nuevoUsuario)
                    .build();
            nuevoUsuario.setPerfilProfesor(perfil);
        }

        return usuarioRepository.save(nuevoUsuario);
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Transactional
    public Usuario actualizarUsuario(Long id, UsuarioUpdateDTO request) {

        Usuario usuario = obtenerUsuarioPorId(id);

        //  CAMBIO: Actualización de Nombres y Apellidos
        if (StringUtils.hasText(request.getNombres())) {
            usuario.setNombres(request.getNombres());
        }
        if (StringUtils.hasText(request.getApellidos())) {
            usuario.setApellidos(request.getApellidos());
        }
        if (StringUtils.hasText(request.getPassword())) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(request.getEmail())) {
                throw new ValidacionException("El nuevo correo electrónico ya está en uso.");
            }
            usuario.setEmail(request.getEmail());
        }

        // --- Actualizar Perfiles ---
        if (usuario.getRol() == Rol.ALUMNO) {
            PerfilAlumno perfil = Optional.ofNullable(usuario.getPerfilAlumno()).orElse(new PerfilAlumno());
            perfil.setUsuario(usuario);
            usuario.setPerfilAlumno(perfil);

            if (StringUtils.hasText(request.getDniAlumno()) && !request.getDniAlumno().equals(perfil.getDni())) {
                if (perfilAlumnoRepository.existsByDni(request.getDniAlumno())) {
                    throw new ValidacionException("El nuevo DNI de alumno ya está en uso.");
                }
                perfil.setDni(request.getDniAlumno());
            }
            if (request.getNivel() != null) perfil.setNivel(request.getNivel());
            if (StringUtils.hasText(request.getGrado())) perfil.setGrado(request.getGrado());
            if (StringUtils.hasText(request.getCodigoEstudiante())) perfil.setCodigoEstudiante(request.getCodigoEstudiante());
        }
        else if (usuario.getRol() == Rol.PROFESOR) {
            PerfilProfesor perfil = Optional.ofNullable(usuario.getPerfilProfesor()).orElse(new PerfilProfesor());
            perfil.setUsuario(usuario);
            usuario.setPerfilProfesor(perfil);

            if (StringUtils.hasText(request.getDniProfesor()) && !request.getDniProfesor().equals(perfil.getDni())) {
                if (perfilProfesorRepository.existsByDni(request.getDniProfesor())) {
                    throw new ValidacionException("El nuevo DNI de profesor ya está en uso.");
                }
                perfil.setDni(request.getDniProfesor());
            }
            if (StringUtils.hasText(request.getTelefono())) perfil.setTelefono(request.getTelefono());
            if (StringUtils.hasText(request.getExperiencia())) perfil.setExperiencia(request.getExperiencia());
            if (StringUtils.hasText(request.getGradoAcademico())) perfil.setGradoAcademico(request.getGradoAcademico());
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + id));

        if (usuario.getRol() == Rol.ALUMNO) {
            if (usuario.getMatriculas() != null && !usuario.getMatriculas().isEmpty()) {
                throw new ValidacionException("No se puede eliminar al alumno '" + usuario.getNombres() +
                        "' porque tiene matrículas registradas.");
            }
        } else if (usuario.getRol() == Rol.PROFESOR) {
            if (usuario.getSeccionesAsignadas() != null && !usuario.getSeccionesAsignadas().isEmpty()) {
                throw new ValidacionException("No se puede eliminar al profesor '" + usuario.getNombres() +
                        "' porque tiene secciones asignadas.");
            }
        }
        usuarioRepository.delete(usuario);
    }

    private String generarCodigoEstudianteUnico() {
        String codigoGenerado;
        boolean codigoExiste;
        do {
            codigoGenerado = "E-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            codigoExiste = perfilAlumnoRepository.existsByCodigoEstudiante(codigoGenerado);
        } while (codigoExiste);
        return codigoGenerado;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuarioResponsePorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con email: " + email));
        return UsuarioResponse.deEntidad(usuario);
    }
}